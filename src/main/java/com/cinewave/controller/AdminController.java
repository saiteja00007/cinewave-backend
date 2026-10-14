package com.cinewave.controller;

import com.cinewave.dto.*;
import com.cinewave.entity.*;
import com.cinewave.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Complete Admin-only REST Controller for full enterprise CRUD operations:
 * Movies, Theaters, Owners, Screens, Seats, Shows, Bookings, Events, Streams, Offers, Users.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("null")
public class AdminController {

    private final AdminService adminService;
    private final MovieService movieService;
    private final TheaterService theaterService;
    private final ShowService showService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final EventService eventService;
    private final StreamService streamService;
    private final OfferService offerService;
    private final TheaterDirectoryService theaterDirectoryService;
    private final MovieSyncService movieSyncService;

    public AdminController(AdminService adminService,
                           MovieService movieService,
                           TheaterService theaterService,
                           ShowService showService,
                           BookingService bookingService,
                           PaymentService paymentService,
                           EventService eventService,
                           StreamService streamService,
                           OfferService offerService,
                           TheaterDirectoryService theaterDirectoryService,
                           MovieSyncService movieSyncService) {
        this.adminService = adminService;
        this.movieService = movieService;
        this.theaterService = theaterService;
        this.showService = showService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.eventService = eventService;
        this.streamService = streamService;
        this.offerService = offerService;
        this.theaterDirectoryService = theaterDirectoryService;
        this.movieSyncService = movieSyncService;
    }

    // ==========================================
    // 1. Dashboard & Analytics
    // ==========================================

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) Long movieId) {

        List<ShowResponse> shows = showService.getShowsForAdmin(date);
        if (movieId != null) {
            shows = shows.stream().filter(s -> s.getMovieId().equals(movieId)).collect(Collectors.toList());
        }
        if (theaterId != null) {
            shows = shows.stream().filter(s -> s.getTheaterName() != null && s.getScreenName() != null).collect(Collectors.toList());
        }

        List<BookingResponse> bookings = bookingService.getAllBookings();
        if (date != null) {
            bookings = bookings.stream().filter(b -> b.getShowDate() != null && b.getShowDate().isEqual(date)).collect(Collectors.toList());
        }
        if (movieId != null) {
            bookings = bookings.stream().filter(b -> b.getMovieTitle() != null).collect(Collectors.toList());
        }

        int totalBookings = bookings.size();
        long confirmed = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long cancelled = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        BigDecimal grossRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(BookingResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTickets = shows.stream().mapToInt(ShowResponse::getBookedSeatsCount).sum();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalShows", shows.size());
        metrics.put("totalBookings", totalBookings);
        metrics.put("confirmedBookings", confirmed);
        metrics.put("cancelledBookings", cancelled);
        metrics.put("totalTicketsBooked", totalTickets);
        metrics.put("grossRevenue", grossRevenue);
        metrics.put("shows", shows);

        return ResponseEntity.ok(metrics);
    }

    // ==========================================
    // 2. Users CRUD
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(@RequestParam(required = false) String query) {
        if (query != null && !query.isBlank()) {
            return ResponseEntity.ok(adminService.searchUsers(query));
        }
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }

    // ==========================================
    // 3. Theater Owners CRUD
    // ==========================================

    @GetMapping("/owners")
    public ResponseEntity<List<OwnerResponse>> getAllOwners() {
        return ResponseEntity.ok(adminService.getAllOwnersWithTheaters());
    }

    @GetMapping("/theater-owners")
    public ResponseEntity<List<UserResponse>> getTheaterOwnersLegacy() {
        return ResponseEntity.ok(adminService.getTheaterOwners());
    }

    @GetMapping("/owners/{id}")
    public ResponseEntity<OwnerResponse> getOwnerById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getOwnerById(id));
    }

    @PostMapping("/owners")
    public ResponseEntity<OwnerResponse> createOwner(@Valid @RequestBody OwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createOwner(request));
    }

    @PutMapping("/owners/{id}")
    public ResponseEntity<OwnerResponse> updateOwner(@PathVariable Long id, @RequestBody OwnerRequest request) {
        return ResponseEntity.ok(adminService.updateOwner(id, request));
    }

    @PutMapping("/owners/{id}/toggle-status")
    public ResponseEntity<OwnerResponse> toggleOwnerStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleOwnerStatus(id));
    }

    @DeleteMapping("/owners/{id}")
    public ResponseEntity<Void> deactivateOwner(@PathVariable Long id) {
        adminService.deactivateOwner(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. Movies CRUD
    // ==========================================

    @GetMapping("/movies")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @PostMapping("/movies")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }

    @PutMapping("/movies/{id}")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id,
                                                      @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @PutMapping("/movies/{id}/toggle-status")
    public ResponseEntity<MovieResponse> toggleMovieStatus(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.togglePublish(id));
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/movies/sync")
    public ResponseEntity<Map<String, Object>> syncMovies(
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        return ResponseEntity.ok(movieSyncService.syncMoviesFromTmdb(force));
    }

    @GetMapping("/movies/sync-status")
    public ResponseEntity<Map<String, Object>> getMovieSyncStatus() {
        return ResponseEntity.ok(movieSyncService.getSyncStatus());
    }

    // ==========================================
    // 5. Theaters CRUD & Directory Discovery
    // ==========================================

    @GetMapping("/theaters")
    public ResponseEntity<List<TheaterResponse>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    @GetMapping("/theaters/{id}")
    public ResponseEntity<TheaterResponse> getTheaterById(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @PostMapping("/theaters")
    public ResponseEntity<TheaterResponse> createTheater(@Valid @RequestBody TheaterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.createTheater(request));
    }

    @PutMapping("/theaters/{id}")
    public ResponseEntity<TheaterResponse> updateTheater(@PathVariable Long id,
                                                          @Valid @RequestBody TheaterRequest request) {
        return ResponseEntity.ok(theaterService.updateTheater(id, request));
    }

    @PutMapping("/theaters/{id}/toggle-status")
    public ResponseEntity<TheaterResponse> toggleTheaterStatus(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.toggleTheaterStatus(id));
    }

    @DeleteMapping("/theaters/{id}")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/theaters/states")
    public ResponseEntity<List<String>> getTheaterStates() {
        return ResponseEntity.ok(theaterService.getStates());
    }

    @GetMapping("/theaters/districts")
    public ResponseEntity<List<String>> getTheaterDistricts(@RequestParam String state) {
        return ResponseEntity.ok(theaterService.getDistrictsByState(state));
    }

    @GetMapping("/theaters/by-state")
    public ResponseEntity<List<TheaterResponse>> getTheatersByState(@RequestParam String state) {
        return ResponseEntity.ok(theaterService.getTheatersByState(state));
    }

    @PutMapping("/theaters/{id}/assign-owner")
    public ResponseEntity<TheaterResponse> assignOwnerToTheater(@PathVariable Long id, @RequestParam(required = false) Long ownerId) {
        return ResponseEntity.ok(theaterService.assignOwner(id, ownerId));
    }

    @GetMapping("/theaters/directory-status")
    public ResponseEntity<Map<String, Object>> getTheaterDirectoryStatus() {
        return ResponseEntity.ok(theaterDirectoryService.getDirectorySyncStatus());
    }

    @PostMapping("/theaters/sync-directory")
    public ResponseEntity<Map<String, Object>> syncTheaterDirectory() {
        return ResponseEntity.ok(theaterDirectoryService.syncTheatersFromDirectory());
    }

    @PostMapping("/theaters/{id}/approve")
    public ResponseEntity<TheaterResponse> approveDiscoveredTheater(@PathVariable Long id) {
        return ResponseEntity.ok(theaterDirectoryService.approveTheater(id));
    }

    @PostMapping("/theaters/{id}/activate-partner")
    public ResponseEntity<TheaterResponse> activatePartnerTheater(
            @PathVariable Long id,
            @RequestParam(required = false) Long ownerId) {
        return ResponseEntity.ok(theaterDirectoryService.activatePartner(id, ownerId));
    }

    @PostMapping("/theaters/{id}/reject")
    public ResponseEntity<TheaterResponse> rejectDiscoveredTheater(@PathVariable Long id) {
        return ResponseEntity.ok(theaterDirectoryService.rejectTheater(id));
    }

    @PostMapping("/theaters/import-csv")
    public ResponseEntity<Map<String, Object>> importTheatersCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("status", "FAILED");
                err.put("message", "Uploaded file is empty");
                return ResponseEntity.badRequest().body(err);
            }
            return ResponseEntity.ok(theaterDirectoryService.importFromCsv(file.getInputStream()));
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED");
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // ==========================================
    // 6. Screens & Seats CRUD
    // ==========================================

    @GetMapping("/theaters/{theaterId}/screens")
    public ResponseEntity<List<ScreenResponse>> getScreens(@PathVariable Long theaterId) {
        return ResponseEntity.ok(theaterService.getScreensByTheater(theaterId));
    }

    @GetMapping("/screens/{id}")
    public ResponseEntity<ScreenResponse> getScreen(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getScreenById(id));
    }

    @PostMapping("/screens")
    public ResponseEntity<ScreenResponse> createScreen(@Valid @RequestBody ScreenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.createScreen(request));
    }

    @PutMapping("/screens/{id}")
    public ResponseEntity<ScreenResponse> updateScreen(@PathVariable Long id, @RequestBody ScreenUpdateRequest request) {
        return ResponseEntity.ok(theaterService.updateScreen(id, request));
    }

    @DeleteMapping("/screens/{id}")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        theaterService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long screenId) {
        return ResponseEntity.ok(theaterService.getSeatsByScreen(screenId));
    }

    @GetMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getSeatById(id));
    }

    @PostMapping("/seats")
    public ResponseEntity<SeatResponse> createSeat(@Valid @RequestBody SeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.createSeat(request));
    }

    @PostMapping("/screens/{screenId}/seats/bulk")
    public ResponseEntity<List<SeatResponse>> createBulkSeats(
            @PathVariable Long screenId,
            @Valid @RequestBody List<SeatRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(theaterService.createBulkSeats(screenId, requests));
    }

    @PutMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatUpdateRequest request) {
        return ResponseEntity.ok(theaterService.updateSeat(id, request));
    }

    @DeleteMapping("/seats/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        theaterService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 7. Shows CRUD
    // ==========================================

    @GetMapping("/shows")
    public ResponseEntity<List<ShowResponse>> getAdminShows(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showService.getShowsForAdmin(date));
    }

    @GetMapping("/shows/{id}")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @PostMapping("/shows")
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.createShow(request));
    }

    @PutMapping("/shows/{id}")
    public ResponseEntity<ShowResponse> updateShow(@PathVariable Long id,
                                                    @Valid @RequestBody ShowRequest request) {
        return ResponseEntity.ok(showService.updateShow(id, request));
    }

    @PutMapping("/shows/{id}/toggle-status")
    public ResponseEntity<ShowResponse> toggleShowStatus(@PathVariable Long id) {
        return ResponseEntity.ok(showService.toggleShowStatus(id));
    }

    @DeleteMapping("/shows/{id}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 8. Bookings & Payments Inspection
    // ==========================================

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (search != null || status != null || date != null) {
            return ResponseEntity.ok(bookingService.filterBookings(search, status, date));
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingByIdAdmin(id));
    }

    @GetMapping("/bookings/{id}/payments")
    public ResponseEntity<List<PaymentResponse>> getBookingPayments(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsByBookingId(id));
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBookingAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBookingAdmin(id));
    }

    // ==========================================
    // 9. Events CRUD
    // ==========================================

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAll());
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(event));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody Event event) {
        return ResponseEntity.ok(eventService.update(id, event));
    }

    @PutMapping("/events/{id}/toggle-status")
    public ResponseEntity<Event> toggleEventStatus(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.toggleStatus(id));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 10. Streams CRUD
    // ==========================================

    @GetMapping("/streams")
    public ResponseEntity<List<StreamContent>> getAllStreams() {
        return ResponseEntity.ok(streamService.getAll());
    }

    @GetMapping("/streams/{id}")
    public ResponseEntity<StreamContent> getStreamById(@PathVariable Long id) {
        return ResponseEntity.ok(streamService.getById(id));
    }

    @PostMapping("/streams")
    public ResponseEntity<StreamContent> createStream(@Valid @RequestBody StreamContent content) {
        return ResponseEntity.status(HttpStatus.CREATED).body(streamService.create(content));
    }

    @PutMapping("/streams/{id}")
    public ResponseEntity<StreamContent> updateStream(@PathVariable Long id, @Valid @RequestBody StreamContent content) {
        return ResponseEntity.ok(streamService.update(id, content));
    }

    @PutMapping("/streams/{id}/toggle-status")
    public ResponseEntity<StreamContent> toggleStreamStatus(@PathVariable Long id) {
        return ResponseEntity.ok(streamService.toggleStatus(id));
    }

    @DeleteMapping("/streams/{id}")
    public ResponseEntity<Void> deleteStream(@PathVariable Long id) {
        streamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 11. Offers CRUD
    // ==========================================

    @GetMapping("/offers")
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAll());
    }

    @GetMapping("/offers/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getById(id));
    }

    @PostMapping("/offers")
    public ResponseEntity<Offer> createOffer(@Valid @RequestBody Offer offer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.create(offer));
    }

    @PutMapping("/offers/{id}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @Valid @RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.update(id, offer));
    }

    @PutMapping("/offers/{id}/toggle-status")
    public ResponseEntity<Offer> toggleOfferStatus(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.toggleStatus(id));
    }

    @DeleteMapping("/offers/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
