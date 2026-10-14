package com.cinewave.controller;

import com.cinewave.dto.*;
import com.cinewave.entity.*;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.*;
import com.cinewave.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Theater Owner REST Controller.
 * Restricts all operations exclusively to resources belonging to the authenticated owner's assigned theaters.
 * Strict backend ownership verification is performed on every single request.
 */
@RestController
@RequestMapping("/api/owner")
@PreAuthorize("hasAnyRole('THEATER_OWNER', 'ADMIN')")
@SuppressWarnings("null")
public class OwnerController {

    private final TheaterService theaterService;
    private final ShowService showService;
    private final MovieService movieService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;

    public OwnerController(TheaterService theaterService,
                           ShowService showService,
                           MovieService movieService,
                           BookingService bookingService,
                           BookingRepository bookingRepository,
                           UserRepository userRepository,
                           TheaterRepository theaterRepository,
                           ScreenRepository screenRepository,
                           SeatRepository seatRepository,
                           ShowRepository showRepository) {
        this.theaterService = theaterService;
        this.showService = showService;
        this.movieService = movieService;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null) {
            throw new BadRequestException("Unauthenticated request");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    private boolean isGlobalAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    private Theater validateTheaterOwnership(Long theaterId, User user) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + theaterId));
        if (!isGlobalAdmin(user) && (theater.getOwner() == null || !theater.getOwner().getId().equals(user.getId()))) {
            throw new BadRequestException("Access denied: Theater not assigned to your account.");
        }
        return theater;
    }

    private Screen validateScreenOwnership(Long screenId, User user) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + screenId));
        validateTheaterOwnership(screen.getTheater().getId(), user);
        return screen;
    }

    private Seat validateSeatOwnership(Long seatId, User user) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));
        validateScreenOwnership(seat.getScreen().getId(), user);
        return seat;
    }

    private Show validateShowOwnership(Long showId, User user) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));
        validateScreenOwnership(show.getScreen().getId(), user);
        return show;
    }

    // ==========================================
    // 1. Theaters Management
    // ==========================================

    @GetMapping("/theaters")
    public ResponseEntity<List<TheaterResponse>> getMyTheaters(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (isGlobalAdmin(user)) {
            return ResponseEntity.ok(theaterService.getAllTheaters());
        }
        return ResponseEntity.ok(theaterService.getTheatersByOwner(user.getId()));
    }

    @GetMapping("/theaters/{id}")
    public ResponseEntity<TheaterResponse> getMyTheater(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateTheaterOwnership(id, user);
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @PutMapping("/theaters/{id}")
    public ResponseEntity<TheaterResponse> updateMyTheater(
            @PathVariable Long id,
            @Valid @RequestBody TheaterRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        Theater theater = validateTheaterOwnership(id, user);

        // Owners can update address and facilities
        if (request.getAddress() != null) {
            theater.setAddress(request.getAddress());
        }
        if (request.getFacilities() != null) {
            theater.setFacilities(request.getFacilities());
        }
        if (isGlobalAdmin(user)) {
            theater.setName(request.getName());
            theater.setCity(request.getCity());
        }
        theater = theaterRepository.save(theater);
        return ResponseEntity.ok(TheaterResponse.fromEntity(theater));
    }

    // ==========================================
    // 2. Approved Movie Catalog (Read-Only)
    // ==========================================

    @GetMapping("/movies")
    public ResponseEntity<List<MovieResponse>> getApprovedMovies() {
        return ResponseEntity.ok(movieService.getAllActiveMovies());
    }

    // ==========================================
    // 3. Screen CRUD for Assigned Theaters
    // ==========================================

    @GetMapping("/theaters/{theaterId}/screens")
    public ResponseEntity<List<ScreenResponse>> getMyTheaterScreens(
            @PathVariable Long theaterId,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateTheaterOwnership(theaterId, user);
        return ResponseEntity.ok(theaterService.getScreensByTheater(theaterId));
    }

    @PostMapping("/theaters/{theaterId}/screens")
    public ResponseEntity<ScreenResponse> createMyScreen(
            @PathVariable Long theaterId,
            @Valid @RequestBody ScreenRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateTheaterOwnership(theaterId, user);
        request.setTheaterId(theaterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.createScreen(request));
    }

    @GetMapping("/screens/{id}")
    public ResponseEntity<ScreenResponse> getMyScreen(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateScreenOwnership(id, user);
        return ResponseEntity.ok(theaterService.getScreenById(id));
    }

    @PutMapping("/screens/{id}")
    public ResponseEntity<ScreenResponse> updateMyScreen(
            @PathVariable Long id,
            @RequestBody ScreenUpdateRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateScreenOwnership(id, user);
        return ResponseEntity.ok(theaterService.updateScreen(id, request));
    }

    @DeleteMapping("/screens/{id}")
    public ResponseEntity<Void> deleteMyScreen(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateScreenOwnership(id, user);
        theaterService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. Seats CRUD for Assigned Screens
    // ==========================================

    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<SeatResponse>> getMySeats(@PathVariable Long screenId, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateScreenOwnership(screenId, user);
        return ResponseEntity.ok(theaterService.getSeatsByScreen(screenId));
    }

    @PostMapping("/screens/{screenId}/seats")
    public ResponseEntity<SeatResponse> createMySeat(
            @PathVariable Long screenId,
            @Valid @RequestBody SeatRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateScreenOwnership(screenId, user);
        request.setScreenId(screenId);
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.createSeat(request));
    }

    @PostMapping("/screens/{screenId}/seats/bulk")
    public ResponseEntity<List<SeatResponse>> createMySeatsBulk(
            @PathVariable Long screenId,
            @RequestBody List<SeatRequest> requests,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateScreenOwnership(screenId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.createBulkSeats(screenId, requests));
    }

    @PutMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> updateMySeat(
            @PathVariable Long id,
            @Valid @RequestBody SeatUpdateRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateSeatOwnership(id, user);
        return ResponseEntity.ok(theaterService.updateSeat(id, request));
    }

    @DeleteMapping("/seats/{id}")
    public ResponseEntity<Void> deleteMySeat(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateSeatOwnership(id, user);
        theaterService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 5. Shows CRUD for Assigned Theaters
    // ==========================================

    @GetMapping("/theaters/{theaterId}/shows")
    public ResponseEntity<List<ShowResponse>> getMyTheaterShows(
            @PathVariable Long theaterId,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateTheaterOwnership(theaterId, user);
        return ResponseEntity.ok(showService.getShowsByTheater(theaterId));
    }

    @GetMapping("/shows")
    public ResponseEntity<List<ShowResponse>> getMyShows(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (isGlobalAdmin(user)) {
            return ResponseEntity.ok(showService.getShowsForAdmin(date));
        }
        return ResponseEntity.ok(showService.getShowsForOwner(user.getId(), date));
    }

    @GetMapping("/shows/{id}")
    public ResponseEntity<ShowResponse> getMyShow(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateShowOwnership(id, user);
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @PostMapping("/shows")
    public ResponseEntity<ShowResponse> createMyShow(
            @Valid @RequestBody ShowRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateScreenOwnership(request.getScreenId(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.createShow(request));
    }

    @PutMapping("/shows/{id}")
    public ResponseEntity<ShowResponse> updateMyShow(
            @PathVariable Long id,
            @Valid @RequestBody ShowRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateShowOwnership(id, user);
        validateScreenOwnership(request.getScreenId(), user);
        return ResponseEntity.ok(showService.updateShow(id, request));
    }

    @PutMapping("/shows/{id}/toggle-status")
    public ResponseEntity<ShowResponse> toggleMyShowStatus(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateShowOwnership(id, user);
        return ResponseEntity.ok(showService.toggleShowStatus(id));
    }

    @DeleteMapping("/shows/{id}")
    public ResponseEntity<Void> deleteMyShow(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateShowOwnership(id, user);
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 6. Bookings & Occupancy Reports
    // ==========================================

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (isGlobalAdmin(user)) {
            return ResponseEntity.ok(bookingService.getAllBookings());
        }
        return ResponseEntity.ok(bookingService.getOwnerBookings(user.getId()));
    }

    @GetMapping("/theaters/{theaterId}/bookings")
    public ResponseEntity<List<BookingResponse>> getMyTheaterBookings(
            @PathVariable Long theaterId,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        validateTheaterOwnership(theaterId, user);
        List<BookingResponse> bookings = bookingRepository.findByTheaterId(theaterId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getMyAnalytics(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        List<Booking> bookings = isGlobalAdmin(user)
                ? bookingRepository.findAll()
                : bookingRepository.findByOwnerId(user.getId());

        List<ShowResponse> shows = isGlobalAdmin(user)
                ? showService.getShowsForAdmin(null)
                : showService.getShowsForOwner(user.getId(), null);

        int totalShows = shows.size();
        int totalBookings = bookings.size();
        long confirmedBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long cancelledBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        BigDecimal grossRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTicketsBooked = shows.stream().mapToInt(ShowResponse::getBookedSeatsCount).sum();

        Map<String, Object> data = new HashMap<>();
        data.put("ownerId", user.getId());
        data.put("ownerName", user.getName());
        data.put("totalTheaters", isGlobalAdmin(user)
                ? theaterService.getAllTheaters().size()
                : theaterService.getTheatersByOwner(user.getId()).size());
        data.put("totalShows", totalShows);
        data.put("totalBookings", totalBookings);
        data.put("confirmedBookings", confirmedBookings);
        data.put("cancelledBookings", cancelledBookings);
        data.put("totalTicketsBooked", totalTicketsBooked);
        data.put("grossRevenue", grossRevenue);

        return ResponseEntity.ok(data);
    }
}
