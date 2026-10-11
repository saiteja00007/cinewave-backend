package com.cinewave.controller;

import com.cinewave.dto.*;
import com.cinewave.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints (protected by Spring Security role check).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final MovieService movieService;
    private final TheaterService theaterService;
    private final ShowService showService;
    private final BookingService bookingService;

    public AdminController(AdminService adminService,
                           MovieService movieService,
                           TheaterService theaterService,
                           ShowService showService,
                           BookingService bookingService) {
        this.adminService = adminService;
        this.movieService = movieService;
        this.theaterService = theaterService;
        this.showService = showService;
        this.bookingService = bookingService;
    }

    // --- Dashboard ---
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // --- Users ---
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // --- Movies CRUD ---
    @GetMapping("/movies")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
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

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    // --- Theaters CRUD ---
    @GetMapping("/theaters")
    public ResponseEntity<List<TheaterResponse>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
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

    @DeleteMapping("/theaters/{id}")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }

    // --- Screens ---
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

    // --- Seats ---
    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long screenId) {
        return ResponseEntity.ok(theaterService.getSeatsByScreen(screenId));
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

    // --- Shows CRUD ---
    @PostMapping("/shows")
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.createShow(request));
    }

    @PutMapping("/shows/{id}")
    public ResponseEntity<ShowResponse> updateShow(@PathVariable Long id,
                                                    @Valid @RequestBody ShowRequest request) {
        return ResponseEntity.ok(showService.updateShow(id, request));
    }

    @DeleteMapping("/shows/{id}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }

    // --- Bookings ---
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(bookingService.searchBookings(search));
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
}
