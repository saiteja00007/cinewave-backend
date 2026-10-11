package com.cinewave.service;

import com.cinewave.dto.MovieRequest;
import com.cinewave.dto.MovieResponse;
import com.cinewave.entity.Movie;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieResponse> getAllActiveMovies() {
        return movieRepository.findByActiveTrue().stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return MovieResponse.fromEntity(movie);
    }

    public List<MovieResponse> searchMovies(String query) {
        return movieRepository.searchMovies(query).stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MovieResponse> filterMovies(String genre, String language) {
        String g = StringUtils.hasText(genre) ? genre : null;
        String l = StringUtils.hasText(language) ? language : null;
        return movieRepository.findByFilters(g, l).stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<String> getGenres() {
        return movieRepository.findDistinctGenres();
    }

    public List<String> getLanguages() {
        return movieRepository.findDistinctLanguages();
    }

    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = new Movie();
        mapRequestToEntity(request, movie);
        movie = movieRepository.save(movie);
        return MovieResponse.fromEntity(movie);
    }

    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        mapRequestToEntity(request, movie);
        movie = movieRepository.save(movie);
        return MovieResponse.fromEntity(movie);
    }

    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        // Soft delete to preserve booking history
        movie.setActive(false);
        movieRepository.save(movie);
    }

    private void mapRequestToEntity(MovieRequest req, Movie movie) {
        movie.setTitle(req.getTitle());
        movie.setDescription(req.getDescription());
        movie.setGenre(req.getGenre());
        movie.setLanguage(req.getLanguage());
        movie.setDuration(req.getDuration());
        movie.setReleaseDate(req.getReleaseDate());
        movie.setRating(req.getRating());
        movie.setCastInfo(req.getCastInfo());
        movie.setPosterUrl(req.getPosterUrl());
        movie.setTrailerUrl(req.getTrailerUrl());
    }
}
