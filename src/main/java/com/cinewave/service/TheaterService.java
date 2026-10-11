package com.cinewave.service;

import com.cinewave.dto.*;
import com.cinewave.entity.*;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    public TheaterService(TheaterRepository theaterRepository,
                          ScreenRepository screenRepository,
                          SeatRepository seatRepository) {
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
    }

    // --- Theater CRUD ---

    public List<TheaterResponse> getAllActiveTheaters() {
        return theaterRepository.findByActiveTrue().stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TheaterResponse> getAllTheaters() {
        return theaterRepository.findAll().stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public TheaterResponse getTheaterById(Long id) {
        Theater t = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + id));
        return TheaterResponse.fromEntity(t);
    }

    public List<TheaterResponse> getTheatersByCity(String city) {
        return theaterRepository.findByCityAndActiveTrue(city).stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<String> getCities() {
        return theaterRepository.findDistinctCities();
    }

    public TheaterResponse createTheater(TheaterRequest request) {
        Theater t = new Theater();
        t.setName(request.getName());
        t.setCity(request.getCity());
        t.setAddress(request.getAddress());
        t.setFacilities(request.getFacilities());
        t = theaterRepository.save(t);
        return TheaterResponse.fromEntity(t);
    }

    public TheaterResponse updateTheater(Long id, TheaterRequest request) {
        Theater t = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + id));
        t.setName(request.getName());
        t.setCity(request.getCity());
        t.setAddress(request.getAddress());
        t.setFacilities(request.getFacilities());
        t = theaterRepository.save(t);
        return TheaterResponse.fromEntity(t);
    }

    public void deleteTheater(Long id) {
        Theater t = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + id));
        t.setActive(false);
        theaterRepository.save(t);
    }

    // --- Screen CRUD ---

    public List<ScreenResponse> getScreensByTheater(Long theaterId) {
        return screenRepository.findByTheaterId(theaterId).stream()
                .map(ScreenResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ScreenResponse getScreenById(Long id) {
        Screen s = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + id));
        return ScreenResponse.fromEntityWithSeats(s);
    }

    public ScreenResponse createScreen(ScreenRequest request) {
        Theater t = theaterRepository.findById(request.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + request.getTheaterId()));
        Screen s = new Screen();
        s.setName(request.getName());
        s.setTheater(t);
        s = screenRepository.save(s);
        return ScreenResponse.fromEntity(s);
    }

    // --- Seat management ---

    public List<SeatResponse> getSeatsByScreen(Long screenId) {
        return seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screenId).stream()
                .map(SeatResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SeatResponse createSeat(SeatRequest request) {
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));
        Seat seat = new Seat(request.getRowLabel(), request.getSeatNumber(),
                             request.getCategory(), screen);
        seat = seatRepository.save(seat);
        return SeatResponse.fromEntity(seat);
    }

    @Transactional
    public List<SeatResponse> createBulkSeats(Long screenId, List<SeatRequest> requests) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + screenId));
        List<Seat> seats = requests.stream().map(req -> {
            Seat seat = new Seat(req.getRowLabel(), req.getSeatNumber(), req.getCategory(), screen);
            return seat;
        }).collect(Collectors.toList());
        seats = seatRepository.saveAll(seats);
        return seats.stream().map(SeatResponse::fromEntity).collect(Collectors.toList());
    }
}
