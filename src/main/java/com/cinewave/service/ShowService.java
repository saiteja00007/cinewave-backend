package com.cinewave.service;

import com.cinewave.dto.ShowRequest;
import com.cinewave.dto.ShowResponse;
import com.cinewave.entity.*;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;

    public ShowService(ShowRepository showRepository,
                       MovieRepository movieRepository,
                       ScreenRepository screenRepository,
                       BookingRepository bookingRepository,
                       BookingSeatRepository bookingSeatRepository,
                       PaymentRepository paymentRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.paymentRepository = paymentRepository;
    }

    private boolean isStartedOrEnded(Show show, LocalDate today, LocalTime now) {
        if (show.getShowDate().isBefore(today)) {
            return true;
        }
        if (show.getShowDate().isEqual(today)) {
            return show.getStartTime().isBefore(now) || show.getStartTime().equals(now);
        }
        return false;
    }

    public List<ShowResponse> getAllUpcomingShows() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return showRepository.findAllUpcoming(today).stream()
                .filter(s -> !isStartedOrEnded(s, today, now))
                .map(ShowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ShowResponse getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));
        return ShowResponse.fromEntity(show);
    }

    public List<ShowResponse> getShowsByMovie(Long movieId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return showRepository.findUpcomingShowsByMovie(movieId, today).stream()
                .filter(s -> !isStartedOrEnded(s, today, now))
                .map(ShowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ShowResponse> getShowsByTheater(Long theaterId) {
        return showRepository.findByTheaterId(theaterId).stream()
                .map(ShowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ShowResponse> getShowsForAdmin(LocalDate date) {
        List<Show> shows;
        if (date != null) {
            shows = showRepository.findByShowDateAndActiveTrueOrderByStartTime(date);
        } else {
            shows = showRepository.findByActiveTrueOrderByShowDateDescStartTimeAsc();
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        return shows.stream().map(s -> {
            ShowResponse dto = ShowResponse.fromEntity(s);
            // Calculate status
            if (s.getShowDate().isBefore(today) || (s.getShowDate().isEqual(today) && s.getEndTime() != null && s.getEndTime().isBefore(now))) {
                dto.setStatus("ENDED");
            } else if (s.getShowDate().isEqual(today) && (s.getStartTime().isBefore(now) || s.getStartTime().equals(now)) && (s.getEndTime() == null || s.getEndTime().isAfter(now))) {
                dto.setStatus("LIVE");
            } else {
                dto.setStatus("UPCOMING");
            }

            // Calculate bookings and revenue
            List<Booking> showBookings = bookingRepository.findByShowIdAndStatus(s.getId(), BookingStatus.CONFIRMED);
            dto.setTotalBookings(showBookings.size());
            BigDecimal collection = showBookings.stream()
                    .map(Booking::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalCollection(collection);

            int bookedSeats = showBookings.stream()
                    .mapToInt(b -> b.getBookingSeats() != null ? b.getBookingSeats().size() : 0)
                    .sum();
            dto.setBookedSeatsCount(bookedSeats);

            return dto;
        }).collect(Collectors.toList());
    }

    public List<ShowResponse> getShowsForOwner(Long ownerId, LocalDate date) {
        List<Show> shows = showRepository.findByOwnerId(ownerId);
        if (date != null) {
            shows = shows.stream().filter(s -> s.getShowDate().isEqual(date)).collect(Collectors.toList());
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        return shows.stream().map(s -> {
            ShowResponse dto = ShowResponse.fromEntity(s);
            if (s.getShowDate().isBefore(today) || (s.getShowDate().isEqual(today) && s.getEndTime() != null && s.getEndTime().isBefore(now))) {
                dto.setStatus("ENDED");
            } else if (s.getShowDate().isEqual(today) && (s.getStartTime().isBefore(now) || s.getStartTime().equals(now)) && (s.getEndTime() == null || s.getEndTime().isAfter(now))) {
                dto.setStatus("LIVE");
            } else {
                dto.setStatus("UPCOMING");
            }

            List<Booking> showBookings = bookingRepository.findByShowIdAndStatus(s.getId(), BookingStatus.CONFIRMED);
            dto.setTotalBookings(showBookings.size());
            BigDecimal collection = showBookings.stream()
                    .map(Booking::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalCollection(collection);

            int bookedSeats = showBookings.stream()
                    .mapToInt(b -> b.getBookingSeats() != null ? b.getBookingSeats().size() : 0)
                    .sum();
            dto.setBookedSeatsCount(bookedSeats);

            return dto;
        }).collect(Collectors.toList());
    }

    public ShowResponse createShow(ShowRequest request) {
        // Validate movie
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + request.getMovieId()));
        if (!movie.isActive()) {
            throw new BadRequestException("Cannot schedule show for inactive movie");
        }

        // Validate screen
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));

        // Auto-calculate end time if not specified: movie duration + 20 min interval
        LocalTime endTime = request.getEndTime();
        if (endTime == null) {
            int durationMinutes = movie.getDuration() != null ? movie.getDuration() : 120;
            endTime = request.getStartTime().plusMinutes(durationMinutes + 20);
        }

        // Validate times
        if (endTime.isBefore(request.getStartTime()) || endTime.equals(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Check for past shows
        if (request.getShowDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot schedule shows in the past");
        }

        // Check for overlapping shows on the same screen
        List<Show> overlapping = showRepository.findOverlappingShows(
                request.getScreenId(), request.getShowDate(),
                request.getStartTime(), endTime);
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Show overlaps with an existing show on this screen");
        }

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(endTime);
        show.setPriceRegular(request.getPriceRegular());
        show.setPricePremium(request.getPricePremium());
        show.setPriceRecliner(request.getPriceRecliner());
        show.setActive(true);

        show = showRepository.save(show);
        return ShowResponse.fromEntity(show);
    }

    public ShowResponse createShowForOwner(ShowRequest request, Long ownerId) {
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));
        if (screen.getTheater().getOwner() == null || !screen.getTheater().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Unauthorized: Screen does not belong to your assigned theater");
        }
        return createShow(request);
    }

    public ShowResponse updateShow(Long id, ShowRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + request.getMovieId()));
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));

        LocalTime endTime = request.getEndTime();
        if (endTime == null) {
            int durationMinutes = movie.getDuration() != null ? movie.getDuration() : 120;
            endTime = request.getStartTime().plusMinutes(durationMinutes + 20);
        }

        if (endTime.isBefore(request.getStartTime()) || endTime.equals(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Check overlaps excluding this show
        List<Show> overlapping = showRepository.findOverlappingShows(
                request.getScreenId(), request.getShowDate(),
                request.getStartTime(), endTime);
        overlapping.removeIf(s -> s.getId().equals(id));
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Show overlaps with an existing show on this screen");
        }

        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(endTime);
        show.setPriceRegular(request.getPriceRegular());
        show.setPricePremium(request.getPricePremium());
        show.setPriceRecliner(request.getPriceRecliner());

        show = showRepository.save(show);
        return ShowResponse.fromEntity(show);
    }

    public ShowResponse updateShowForOwner(Long id, ShowRequest request, Long ownerId) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));
        if (show.getScreen().getTheater().getOwner() == null || !show.getScreen().getTheater().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Unauthorized: Show does not belong to your assigned theater");
        }
        Screen targetScreen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));
        if (targetScreen.getTheater().getOwner() == null || !targetScreen.getTheater().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Unauthorized: Screen does not belong to your assigned theater");
        }
        return updateShow(id, request);
    }

    public ShowResponse toggleShowStatus(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));
        show.setActive(!show.isActive());
        show = showRepository.save(show);
        return ShowResponse.fromEntity(show);
    }

    public ShowResponse toggleShowStatusForOwner(Long id, Long ownerId) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));
        if (show.getScreen().getTheater().getOwner() == null || !show.getScreen().getTheater().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Unauthorized: Show does not belong to your assigned theater");
        }
        return toggleShowStatus(id);
    }

    public void deleteShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));

        // Handle affected bookings safely: cancel bookings, release seats, mark refunds
        List<Booking> bookings = bookingRepository.findByShowIdAndStatus(id, BookingStatus.CONFIRMED);
        for (Booking booking : bookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(LocalDateTime.now());
            booking.setPaymentStatus("REFUNDED");
            bookingRepository.save(booking);

            List<BookingSeat> seats = bookingSeatRepository.findByBookingId(booking.getId());
            if (seats != null && !seats.isEmpty()) {
                bookingSeatRepository.deleteAll(seats);
            }

            // Update payment transaction record to REFUNDED
            List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
            for (Payment p : payments) {
                p.setStatus("REFUNDED");
                paymentRepository.save(p);
            }
        }

        show.setActive(false);
        showRepository.save(show);
    }

    public void deleteShowForOwner(Long id, Long ownerId) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));
        if (show.getScreen().getTheater().getOwner() == null || !show.getScreen().getTheater().getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Unauthorized: Show does not belong to your assigned theater");
        }
        deleteShow(id);
    }
}
