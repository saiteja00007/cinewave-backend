package com.cinewave.controller;

import com.cinewave.dto.TheaterResponse;
import com.cinewave.service.TheaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
public class TheaterController {

    private final TheaterService theaterService;

    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @GetMapping
    public ResponseEntity<List<TheaterResponse>> getTheaters(
            @RequestParam(required = false) String city) {
        List<TheaterResponse> theaters;
        if (city != null && !city.isBlank()) {
            theaters = theaterService.getTheatersByCity(city);
        } else {
            theaters = theaterService.getAllActiveTheaters();
        }
        return ResponseEntity.ok(theaters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheaterResponse> getTheater(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(theaterService.getCities());
    }
}
