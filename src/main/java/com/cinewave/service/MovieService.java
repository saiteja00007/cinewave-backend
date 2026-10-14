package com.cinewave.service;

import com.cinewave.dto.MovieRequest;
import com.cinewave.dto.MovieResponse;
import com.cinewave.entity.Movie;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    public List<MovieResponse> getNowPlayingMovies() {
        List<Movie> list = movieRepository.findByTagAndActiveTrue("NOW_PLAYING");
        if (list.isEmpty()) {
            list = movieRepository.findByReleaseDateLessThanEqualAndActiveTrueOrderByReleaseDateDesc(LocalDate.now());
        }
        return list.stream().map(MovieResponse::fromEntity).collect(Collectors.toList());
    }

    public List<MovieResponse> getUpcomingMovies() {
        List<Movie> list = movieRepository.findByTagAndActiveTrue("UPCOMING");
        if (list.isEmpty()) {
            list = movieRepository.findByReleaseDateGreaterThanAndActiveTrueOrderByReleaseDateAsc(LocalDate.now());
        }
        return list.stream().map(MovieResponse::fromEntity).collect(Collectors.toList());
    }

    public List<MovieResponse> getPopularMovies() {
        List<Movie> list = movieRepository.findByTagAndActiveTrue("POPULAR");
        if (list.isEmpty()) {
            list = movieRepository.findAllByActiveTrueOrderByVoteAverageDescVoteCountDesc();
        }
        return list.stream().limit(12).map(MovieResponse::fromEntity).collect(Collectors.toList());
    }

    public List<MovieResponse> getMoviesByTag(String tag) {
        if (!StringUtils.hasText(tag)) {
            return getAllActiveMovies();
        }
        return movieRepository.findByTagAndActiveTrue(tag.trim().toUpperCase()).stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MovieResponse> getMoviesByLanguage(String lang) {
        if (!StringUtils.hasText(lang) || lang.equalsIgnoreCase("all")) {
            return getAllActiveMovies();
        }
        String l = lang.trim();
        List<Movie> list = movieRepository.findByOriginalLanguageIgnoreCaseAndActiveTrue(l);
        if (list.isEmpty()) {
            list = movieRepository.findByLanguageContainingIgnoreCaseAndActiveTrue(l);
        }
        return list.stream().map(MovieResponse::fromEntity).collect(Collectors.toList());
    }

    public List<String> getGenres() {
        return movieRepository.findDistinctGenres();
    }

    public List<String> getLanguages() {
        return movieRepository.findDistinctLanguages();
    }

    public MovieResponse createMovie(MovieRequest request) {
        if (request.getTmdbId() != null) {
            Optional<Movie> existing = movieRepository.findByTmdbId(request.getTmdbId());
            if (existing.isPresent()) {
                Movie movie = existing.get();
                mapRequestToEntity(request, movie);
                movie.setCustomizedByAdmin(true);
                movie.setActive(true);
                movie = movieRepository.save(movie);
                return MovieResponse.fromEntity(movie);
            }
        }

        if (StringUtils.hasText(request.getTitle())) {
            Optional<Movie> existingByTitle = movieRepository.findByTitleIgnoreCase(request.getTitle().trim());
            if (existingByTitle.isPresent()) {
                Movie movie = existingByTitle.get();
                mapRequestToEntity(request, movie);
                movie.setCustomizedByAdmin(true);
                movie.setActive(true);
                movie = movieRepository.save(movie);
                return MovieResponse.fromEntity(movie);
            }
        }

        Movie movie = new Movie();
        mapRequestToEntity(request, movie);
        movie.setCustomizedByAdmin(true);
        movie.setActive(true);
        movie = movieRepository.save(movie);
        return MovieResponse.fromEntity(movie);
    }

    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        mapRequestToEntity(request, movie);
        movie.setCustomizedByAdmin(true);
        movie = movieRepository.save(movie);
        return MovieResponse.fromEntity(movie);
    }

    public MovieResponse togglePublish(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movie.setActive(!movie.isActive());
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
        if (StringUtils.hasText(req.getOriginalLanguage())) {
            movie.setOriginalLanguage(req.getOriginalLanguage());
        }
        if (StringUtils.hasText(req.getTag())) {
            movie.setTag(req.getTag());
        }
        movie.setDuration(req.getDuration());
        movie.setReleaseDate(req.getReleaseDate());
        movie.setRating(req.getRating());
        movie.setCastInfo(req.getCastInfo());
        movie.setPosterUrl(req.getPosterUrl());
        movie.setTrailerUrl(req.getTrailerUrl());
        movie.setTmdbId(req.getTmdbId());
        movie.setBackdropUrl(req.getBackdropUrl());
        movie.setVoteAverage(req.getVoteAverage());
        movie.setVoteCount(req.getVoteCount());
        movie.setDirector(req.getDirector());
    }
}
