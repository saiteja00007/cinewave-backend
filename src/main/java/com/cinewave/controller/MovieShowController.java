package com.cinewave.controller;

import com.cinewave.dto.ShowResponse;
import com.cinewave.service.ShowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Nested endpoint: GET /api/movies/{movieId}/shows
 */
@RestController
@RequestMapping("/api/movies/{movieId}/shows")
public class MovieShowController {

    private final ShowService showService;

    public MovieShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping
    public ResponseEntity<List<ShowResponse>> getShowsByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(showService.getShowsByMovie(movieId));
    }
}
