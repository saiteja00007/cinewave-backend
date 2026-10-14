package com.cinewave.controller;

import com.cinewave.dto.TmdbMovieDetails;
import com.cinewave.dto.TmdbSearchResult;
import com.cinewave.service.MovieSyncService;
import com.cinewave.service.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for admin TMDB movie integration and synchronization.
 * Protected by Spring Security role check (ADMIN only).
 */
@RestController
@RequestMapping("/api/admin/tmdb")
@PreAuthorize("hasRole('ADMIN')")
public class TmdbController {

    private final TmdbService tmdbService;
    private final MovieSyncService movieSyncService;

    public TmdbController(TmdbService tmdbService, MovieSyncService movieSyncService) {
        this.tmdbService = tmdbService;
        this.movieSyncService = movieSyncService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean configured = tmdbService.isConfigured();
        status.put("configured", configured);
        status.put("region", "IN");
        status.put("message", configured
                ? "Live TMDB API is active and connected (India region IN)."
                : "TMDB_API_KEY environment variable is not configured. Built-in curated Indian/Global blockbusters catalog is active for offline resilience.");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/sync-status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        return ResponseEntity.ok(movieSyncService.getSyncStatus());
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> triggerSync(
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        return ResponseEntity.ok(movieSyncService.syncMoviesFromTmdb(force));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TmdbSearchResult>> search(@RequestParam String query) {
        return ResponseEntity.ok(tmdbService.searchMovies(query));
    }

    @GetMapping("/details/{tmdbId}")
    public ResponseEntity<TmdbMovieDetails> getDetails(@PathVariable Long tmdbId) {
        return ResponseEntity.ok(tmdbService.getMovieDetails(tmdbId));
    }

    @GetMapping("/now-playing")
    public ResponseEntity<List<TmdbSearchResult>> getNowPlaying(
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(tmdbService.getNowPlaying(page));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<TmdbSearchResult>> getUpcoming(
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(tmdbService.getUpcoming(page));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<TmdbSearchResult>> getPopular(
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(tmdbService.getPopular(page));
    }

    @GetMapping("/language")
    public ResponseEntity<List<TmdbSearchResult>> getByLanguage(
            @RequestParam String lang,
            @RequestParam(required = false, defaultValue = "1") int page) {
        return ResponseEntity.ok(tmdbService.discoverByLanguage(lang, page));
    }
}
