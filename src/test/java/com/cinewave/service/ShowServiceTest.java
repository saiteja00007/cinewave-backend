package com.cinewave.service;

import com.cinewave.dto.ShowRequest;
import com.cinewave.dto.ShowResponse;
import com.cinewave.entity.Movie;
import com.cinewave.entity.Screen;
import com.cinewave.entity.Show;
import com.cinewave.entity.Theater;
import com.cinewave.exception.BadRequestException;
import com.cinewave.repository.MovieRepository;
import com.cinewave.repository.ScreenRepository;
import com.cinewave.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScreenRepository screenRepository;

    @InjectMocks
    private ShowService showService;

    private Movie sampleMovie;
    private Screen sampleScreen;

    @BeforeEach
    void setUp() {
        sampleMovie = new Movie();
        sampleMovie.setId(10L);
        sampleMovie.setTitle("Avatar");
        sampleMovie.setDuration(160);
        sampleMovie.setActive(true);

        Theater theater = new Theater();
        theater.setId(5L);
        theater.setName("PVR City");
        theater.setCity("Mumbai");

        sampleScreen = new Screen();
        sampleScreen.setId(20L);
        sampleScreen.setName("Screen 1");
        sampleScreen.setTheater(theater);
    }

    @Test
    void createShow_Success_WithAutoEndTime() {
        ShowRequest req = new ShowRequest();
        req.setMovieId(10L);
        req.setScreenId(20L);
        req.setShowDate(LocalDate.now().plusDays(1));
        req.setStartTime(LocalTime.of(14, 0));
        req.setPriceRegular(BigDecimal.valueOf(200));
        req.setPricePremium(BigDecimal.valueOf(300));
        req.setPriceRecliner(BigDecimal.valueOf(500));

        when(movieRepository.findById(10L)).thenReturn(Optional.of(sampleMovie));
        when(screenRepository.findById(20L)).thenReturn(Optional.of(sampleScreen));
        when(showRepository.findOverlappingShows(eq(20L), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> {
            Show s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        ShowResponse res = showService.createShow(req);

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("Avatar", res.getMovieTitle());
        // Duration is 160 + 20 buffer = 180 min (3 hours) -> 14:00 to 17:00
        assertEquals(LocalTime.of(17, 0), res.getEndTime());
        verify(showRepository).save(any(Show.class));
    }

    @Test
    void createShow_OverlappingShow_ThrowsBadRequestException() {
        ShowRequest req = new ShowRequest();
        req.setMovieId(10L);
        req.setScreenId(20L);
        req.setShowDate(LocalDate.now().plusDays(1));
        req.setStartTime(LocalTime.of(14, 0));
        req.setPriceRegular(BigDecimal.valueOf(200));
        req.setPricePremium(BigDecimal.valueOf(300));
        req.setPriceRecliner(BigDecimal.valueOf(500));

        when(movieRepository.findById(10L)).thenReturn(Optional.of(sampleMovie));
        when(screenRepository.findById(20L)).thenReturn(Optional.of(sampleScreen));
        when(showRepository.findOverlappingShows(eq(20L), any(), any(), any()))
                .thenReturn(List.of(new Show()));

        assertThrows(BadRequestException.class, () -> showService.createShow(req));
        verify(showRepository, never()).save(any());
    }

    @Test
    void createShow_PastDate_ThrowsBadRequestException() {
        ShowRequest req = new ShowRequest();
        req.setMovieId(10L);
        req.setScreenId(20L);
        req.setShowDate(LocalDate.now().minusDays(1)); // past date
        req.setStartTime(LocalTime.of(14, 0));
        req.setPriceRegular(BigDecimal.valueOf(200));
        req.setPricePremium(BigDecimal.valueOf(300));
        req.setPriceRecliner(BigDecimal.valueOf(500));

        when(movieRepository.findById(10L)).thenReturn(Optional.of(sampleMovie));
        when(screenRepository.findById(20L)).thenReturn(Optional.of(sampleScreen));

        assertThrows(BadRequestException.class, () -> showService.createShow(req));
    }
}
