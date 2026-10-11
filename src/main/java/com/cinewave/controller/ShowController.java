package com.cinewave.controller;

import com.cinewave.dto.SeatResponse;
import com.cinewave.dto.ShowResponse;
import com.cinewave.service.BookingService;
import com.cinewave.service.ShowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;
    private final BookingService bookingService;

    public ShowController(ShowService showService, BookingService bookingService) {
        this.showService = showService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<ShowResponse>> getAllShows() {
        return ResponseEntity.ok(showService.getAllUpcomingShows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowResponse> getShow(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<SeatResponse>> getShowSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(bookingService.getShowSeats(showId));
    }
}
