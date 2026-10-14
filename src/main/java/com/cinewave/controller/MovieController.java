package com.cinewave.controller;

import com.cinewave.dto.MovieResponse;
import com.cinewave.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String tag) {

        List<MovieResponse> movies;
        if (search != null && !search.isBlank()) {
            movies = movieService.searchMovies(search);
        } else if (tag != null && !tag.isBlank()) {
            movies = movieService.getMoviesByTag(tag);
        } else if (genre != null || language != null) {
            movies = movieService.filterMovies(genre, language);
        } else {
            movies = movieService.getAllActiveMovies();
        }
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/now-playing")
    public ResponseEntity<List<MovieResponse>> getNowPlaying() {
        return ResponseEntity.ok(movieService.getNowPlayingMovies());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MovieResponse>> getUpcoming() {
        return ResponseEntity.ok(movieService.getUpcomingMovies());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MovieResponse>> getPopular() {
        return ResponseEntity.ok(movieService.getPopularMovies());
    }

    @GetMapping("/by-language")
    public ResponseEntity<List<MovieResponse>> getByLanguage(@RequestParam String lang) {
        return ResponseEntity.ok(movieService.getMoviesByLanguage(lang));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<MovieResponse> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getGenres() {
        return ResponseEntity.ok(movieService.getGenres());
    }

    @GetMapping("/languages")
    public ResponseEntity<List<String>> getLanguages() {
        return ResponseEntity.ok(movieService.getLanguages());
    }
}
