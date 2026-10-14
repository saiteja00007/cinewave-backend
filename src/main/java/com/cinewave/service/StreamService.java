package com.cinewave.service;

import com.cinewave.entity.StreamContent;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.StreamContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class StreamService {

    private final StreamContentRepository streamRepository;

    public StreamService(StreamContentRepository streamRepository) {
        this.streamRepository = streamRepository;
    }

    public List<StreamContent> getStreams(String category, String query) {
        if (query != null && !query.trim().isEmpty()) {
            return streamRepository.findByTitleContainingIgnoreCaseAndActiveTrue(query.trim());
        }
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            return streamRepository.findByCategoryIgnoreCaseAndActiveTrue(category.trim());
        }
        return streamRepository.findByActiveTrue();
    }

    public StreamContent getById(Long id) {
        return streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stream content not found with id: " + id));
    }

    @Transactional
    public StreamContent create(StreamContent content) {
        return streamRepository.save(content);
    }

    @Transactional
    public StreamContent update(Long id, StreamContent updated) {
        StreamContent existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setLanguage(updated.getLanguage());
        existing.setDuration(updated.getDuration());
        existing.setRating(updated.getRating());
        existing.setThumbnailUrl(updated.getThumbnailUrl());
        existing.setBackdropUrl(updated.getBackdropUrl());
        existing.setVideoUrl(updated.getVideoUrl());
        existing.setPrice(updated.getPrice());
        existing.setActive(updated.isActive());
        existing.setReleaseDate(updated.getReleaseDate());
        return streamRepository.save(existing);
    }

    public List<StreamContent> getAll() {
        return streamRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        StreamContent existing = getById(id);
        existing.setActive(false);
        streamRepository.save(existing);
    }

    @Transactional
    public StreamContent toggleStatus(Long id) {
        StreamContent existing = getById(id);
        existing.setActive(!existing.isActive());
        return streamRepository.save(existing);
    }
}
