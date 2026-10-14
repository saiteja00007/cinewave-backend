package com.cinewave.service;

import com.cinewave.dto.MovieRequest;
import com.cinewave.dto.MovieResponse;
import com.cinewave.entity.Movie;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie sampleMovie;

    @BeforeEach
    void setUp() {
        sampleMovie = new Movie();
        sampleMovie.setId(1L);
        sampleMovie.setTitle("Inception");
        sampleMovie.setDescription("Dream heist thriller");
        sampleMovie.setGenre("Sci-Fi / Action");
        sampleMovie.setLanguage("English");
        sampleMovie.setDuration(148);
        sampleMovie.setRating("PG-13");
        sampleMovie.setPosterUrl("https://example.com/poster.jpg");
        sampleMovie.setActive(true);
    }

    @Test
    void getAllActiveMovies_ReturnsList() {
        when(movieRepository.findByActiveTrue()).thenReturn(List.of(sampleMovie));

        List<MovieResponse> list = movieService.getAllActiveMovies();

        assertEquals(1, list.size());
        assertEquals("Inception", list.get(0).getTitle());
    }

    @Test
    void getMovieById_Success() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(sampleMovie));

        MovieResponse res = movieService.getMovieById(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
        assertEquals("Inception", res.getTitle());
    }

    @Test
    void getMovieById_NotFound_ThrowsException() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(99L));
    }

    @Test
    void createMovie_Success() {
        MovieRequest req = new MovieRequest();
        req.setTitle("Interstellar");
        req.setDescription("Space exploration epic");
        req.setGenre("Sci-Fi");
        req.setLanguage("English");
        req.setDuration(169);
        req.setRating("PG-13");

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            m.setId(2L);
            return m;
        });

        MovieResponse res = movieService.createMovie(req);

        assertNotNull(res);
        assertEquals(2L, res.getId());
        assertEquals("Interstellar", res.getTitle());
    }

    @Test
    void deleteMovie_SetsActiveFalse() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(sampleMovie));

        movieService.deleteMovie(1L);

        assertFalse(sampleMovie.isActive());
        verify(movieRepository).save(sampleMovie);
    }
}
