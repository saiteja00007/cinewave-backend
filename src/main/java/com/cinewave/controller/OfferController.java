package com.cinewave.controller;

import com.cinewave.dto.OfferValidationRequest;
import com.cinewave.dto.OfferValidationResponse;
import com.cinewave.entity.Offer;
import com.cinewave.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public ResponseEntity<List<Offer>> getActiveOffers() {
        return ResponseEntity.ok(offerService.getAllActive());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getById(id));
    }

    @PostMapping("/validate")
    public ResponseEntity<OfferValidationResponse> validatePromo(
            @Valid @RequestBody OfferValidationRequest request) {
        return ResponseEntity.ok(offerService.validatePromo(request));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Offer> createOffer(@Valid @RequestBody Offer offer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.create(offer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @Valid @RequestBody Offer offer) {
        return ResponseEntity.ok(offerService.update(id, offer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
