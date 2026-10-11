package com.cinewave.service;

import com.cinewave.dto.ScreenRequest;
import com.cinewave.dto.ScreenResponse;
import com.cinewave.dto.SeatRequest;
import com.cinewave.dto.SeatResponse;
import com.cinewave.dto.TheaterRequest;
import com.cinewave.dto.TheaterResponse;
import com.cinewave.entity.Role;
import com.cinewave.entity.Screen;
import com.cinewave.entity.Seat;
import com.cinewave.entity.SeatCategory;
import com.cinewave.entity.Theater;
import com.cinewave.repository.ScreenRepository;
import com.cinewave.repository.SeatRepository;
import com.cinewave.repository.TheaterRepository;
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
class TheaterServiceTest {

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private TheaterService theaterService;

    private Theater sampleTheater;

    @BeforeEach
    void setUp() {
        sampleTheater = new Theater();
        sampleTheater.setId(1L);
        sampleTheater.setName("CineWave Gold");
        sampleTheater.setCity("Bengaluru");
        sampleTheater.setAddress("MG Road");
        sampleTheater.setActive(true);
    }

    @Test
    void getTheatersByCity_ReturnsFilteredList() {
        when(theaterRepository.findByCityAndActiveTrue("Bengaluru"))
                .thenReturn(List.of(sampleTheater));

        List<TheaterResponse> list = theaterService.getTheatersByCity("Bengaluru");

        assertEquals(1, list.size());
        assertEquals("CineWave Gold", list.get(0).getName());
        assertEquals("Bengaluru", list.get(0).getCity());
    }

    @Test
    void createTheater_Success() {
        TheaterRequest req = new TheaterRequest();
        req.setName("CineWave IMAX");
        req.setCity("Delhi");
        req.setAddress("Connaught Place");

        when(theaterRepository.save(any(Theater.class))).thenAnswer(invocation -> {
            Theater t = invocation.getArgument(0);
            t.setId(2L);
            return t;
        });

        TheaterResponse res = theaterService.createTheater(req);

        assertNotNull(res);
        assertEquals(2L, res.getId());
        assertEquals("CineWave IMAX", res.getName());
    }

    @Test
    void createScreen_Success() {
        ScreenRequest req = new ScreenRequest();
        req.setTheaterId(1L);
        req.setName("Audi 1 IMAX");

        when(theaterRepository.findById(1L)).thenReturn(Optional.of(sampleTheater));
        when(screenRepository.save(any(Screen.class))).thenAnswer(invocation -> {
            Screen s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        ScreenResponse res = theaterService.createScreen(req);

        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals("Audi 1 IMAX", res.getName());
    }

    @Test
    void createBulkSeats_Success() {
        Screen screen = new Screen();
        screen.setId(10L);
        screen.setName("Audi 1");

        SeatRequest req1 = new SeatRequest();
        req1.setScreenId(10L);
        req1.setRowLabel("A");
        req1.setSeatNumber(1);
        req1.setCategory(SeatCategory.RECLINER);

        SeatRequest req2 = new SeatRequest();
        req2.setScreenId(10L);
        req2.setRowLabel("A");
        req2.setSeatNumber(2);
        req2.setCategory(SeatCategory.RECLINER);

        when(screenRepository.findById(10L)).thenReturn(Optional.of(screen));
        when(seatRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatResponse> seats = theaterService.createBulkSeats(10L, List.of(req1, req2));

        assertEquals(2, seats.size());
        assertEquals("A", seats.get(0).getRowLabel());
        assertEquals(1, seats.get(0).getSeatNumber());
        assertEquals(SeatCategory.RECLINER, seats.get(0).getCategory());
    }
}
