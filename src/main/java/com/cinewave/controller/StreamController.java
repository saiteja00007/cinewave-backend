package com.cinewave.controller;

import com.cinewave.entity.StreamContent;
import com.cinewave.service.StreamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/streams")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    @GetMapping
    public ResponseEntity<List<StreamContent>> getStreams(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(streamService.getStreams(category, query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StreamContent> getStreamById(@PathVariable Long id) {
        return ResponseEntity.ok(streamService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamContent> createStream(@Valid @RequestBody StreamContent content) {
        return ResponseEntity.status(HttpStatus.CREATED).body(streamService.create(content));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamContent> updateStream(
            @PathVariable Long id,
            @Valid @RequestBody StreamContent content) {
        return ResponseEntity.ok(streamService.update(id, content));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStream(@PathVariable Long id) {
        streamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
