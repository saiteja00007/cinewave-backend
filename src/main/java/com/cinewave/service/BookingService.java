package com.cinewave.service;

import com.cinewave.dto.*;
import com.cinewave.entity.*;
import com.cinewave.exception.*;
import com.cinewave.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core booking service with transactional seat reservation and concurrency safety.
 *
 * Concurrency strategy:
 * 1. Database unique constraint on (show_id, seat_id) in booking_seats table
 *    prevents double-booking at the DB level.
 * 2. @Transactional ensures atomicity — all seats are booked or none.
 * 3. We check seat availability first, then insert. If a concurrent transaction
 *    already inserted, the unique constraint violation causes a rollback.
 * 4. Prices are always calculated server-side, never trusted from the frontend.
 */
@Service
@Transactional
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          BookingSeatRepository bookingSeatRepository,
                          ShowRepository showRepository,
                          SeatRepository seatRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new booking atomically.
     * If any seat is unavailable, the entire booking is rejected.
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + request.getShowId()));

        // Validate show is active and in the future
        if (!show.isActive()) {
            throw new BadRequestException("This show is no longer available");
        }
        LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getStartTime());
        if (showDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book a show that has already started");
        }

        // Validate seats
        List<Long> seatIds = request.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            throw new BadRequestException("At least one seat must be selected");
        }

        // Remove duplicates
        seatIds = seatIds.stream().distinct().collect(Collectors.toList());

        // Load all requested seats
        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BadRequestException("One or more selected seats do not exist");
        }

        // Validate all seats belong to the show's screen
        Long screenId = show.getScreen().getId();
        for (Seat seat : seats) {
            if (!seat.getScreen().getId().equals(screenId)) {
                throw new BadRequestException("Seat " + seat.getRowLabel() + seat.getSeatNumber() +
                        " does not belong to this screen");
            }
        }

        // Check seat availability (pre-check, DB constraint is the final guard)
        boolean anyBooked = bookingSeatRepository.areAnySeatsBooked(show.getId(), seatIds);
        if (anyBooked) {
            throw new ConflictException("One or more selected seats are already booked");
        }

        // Calculate total price server-side
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Seat seat : seats) {
            totalAmount = totalAmount.add(show.getPriceForCategory(seat.getCategory()));
        }

        // Generate unique booking reference
        String bookingRef = generateBookingReference();

        // Create booking
        Booking booking = new Booking();
        booking.setBookingReference(bookingRef);
        booking.setUser(user);
        booking.setShow(show);
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        // Create booking seats (DB unique constraint prevents double-booking)
        for (Seat seat : seats) {
            BookingSeat bs = new BookingSeat(
                    booking, show, seat,
                    show.getPriceForCategory(seat.getCategory())
            );
            bookingSeatRepository.save(bs);
        }

        // Reload with relationships for response
        booking = bookingRepository.findById(booking.getId()).orElseThrow();
        booking.setBookingSeats(bookingSeatRepository.findByBookingId(booking.getId()));

        log.info("Booking created: {} for user: {}", bookingRef, userEmail);
        return BookingResponse.fromEntity(booking);
    }

    public List<BookingResponse> getUserBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long id, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        // Users can only see their own bookings (admins bypass this via controller)
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("Access denied to this booking");
        }

        booking.setBookingSeats(bookingSeatRepository.findByBookingId(booking.getId()));
        return BookingResponse.fromEntity(booking);
    }

    /**
     * Cancel a booking. Cancellation policy:
     * - Can cancel up to 2 hours before showtime
     * - Cannot cancel already cancelled bookings
     */
    @Transactional
    public BookingResponse cancelBooking(Long id, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("Access denied to this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        // Cancellation policy: must be at least 2 hours before show
        LocalDateTime showDateTime = LocalDateTime.of(
                booking.getShow().getShowDate(),
                booking.getShow().getStartTime());
        if (LocalDateTime.now().plusHours(2).isAfter(showDateTime)) {
            throw new BadRequestException("Cancellation is not allowed within 2 hours of the show");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // The booking_seats remain but are no longer counted as CONFIRMED,
        // so those seats become available for new bookings
        // (our queries filter by booking status = CONFIRMED)

        booking.setBookingSeats(bookingSeatRepository.findByBookingId(booking.getId()));
        log.info("Booking cancelled: {}", booking.getBookingReference());
        return BookingResponse.fromEntity(booking);
    }

    // --- Admin methods ---

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllOrderByCreatedAtDesc().stream()
                .map(b -> {
                    b.setBookingSeats(bookingSeatRepository.findByBookingId(b.getId()));
                    return BookingResponse.fromEntity(b);
                })
                .collect(Collectors.toList());
    }

    public List<BookingResponse> searchBookings(String query) {
        return bookingRepository.searchBookings(query).stream()
                .map(b -> {
                    b.setBookingSeats(bookingSeatRepository.findByBookingId(b.getId()));
                    return BookingResponse.fromEntity(b);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get seat availability for a specific show.
     * Returns all seats in the screen with their booked/available status.
     */
    public List<SeatResponse> getShowSeats(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        Set<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowId(showId);

        List<Seat> allSeats = seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(
                show.getScreen().getId());

        return allSeats.stream().map(seat -> {
            SeatResponse sr = SeatResponse.fromEntity(seat);
            sr.setBooked(bookedSeatIds.contains(seat.getId()));
            return sr;
        }).collect(Collectors.toList());
    }

    private String generateBookingReference() {
        return "CW" + System.currentTimeMillis() % 100000000L +
                String.format("%04d", new Random().nextInt(10000));
    }
}
