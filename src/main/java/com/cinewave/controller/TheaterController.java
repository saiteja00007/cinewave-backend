package com.cinewave.controller;

import com.cinewave.dto.TheaterResponse;
import com.cinewave.service.TheaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.cinewave.service.TheaterDirectoryService;
import java.util.Map;

@RestController
@RequestMapping("/api/theaters")
public class TheaterController {

    private final TheaterService theaterService;
    private final TheaterDirectoryService theaterDirectoryService;

    public TheaterController(TheaterService theaterService, TheaterDirectoryService theaterDirectoryService) {
        this.theaterService = theaterService;
        this.theaterDirectoryService = theaterDirectoryService;
    }

    @GetMapping("/directory-status")
    public ResponseEntity<Map<String, Object>> getDirectoryStatus() {
        return ResponseEntity.ok(theaterDirectoryService.getDirectorySyncStatus());
    }

    @GetMapping
    public ResponseEntity<List<TheaterResponse>> getTheaters(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean verifiedOnly) {

        List<TheaterResponse> theaters;
        if (search != null && !search.isBlank()) {
            theaters = theaterService.searchTheaters(search);
        } else if (verifiedOnly) {
            if (city != null && !city.isBlank()) {
                theaters = theaterService.getVerifiedTheatersByCity(city);
            } else {
                theaters = theaterService.getVerifiedTheaters();
            }
        } else if (city != null && !city.isBlank()) {
            theaters = theaterService.getTheatersByCity(city);
        } else if (state != null && !state.isBlank()) {
            theaters = theaterService.getTheatersByState(state);
        } else {
            theaters = theaterService.getAllActiveTheaters();
        }
        return ResponseEntity.ok(theaters);
    }

    @GetMapping("/verified")
    public ResponseEntity<List<TheaterResponse>> getVerifiedTheaters(
            @RequestParam(required = false) String city) {
        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(theaterService.getVerifiedTheatersByCity(city));
        }
        return ResponseEntity.ok(theaterService.getVerifiedTheaters());
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<TheaterResponse> getTheater(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(theaterService.getCities());
    }

    @GetMapping("/states")
    public ResponseEntity<List<String>> getStates() {
        return ResponseEntity.ok(theaterService.getStates());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<String>> getDistricts(@RequestParam String state) {
        return ResponseEntity.ok(theaterService.getDistrictsByState(state));
    }

    @GetMapping("/cities-by-state")
    public ResponseEntity<List<String>> getCitiesByState(@RequestParam String state) {
        return ResponseEntity.ok(theaterService.getCitiesByState(state));
    }
}
