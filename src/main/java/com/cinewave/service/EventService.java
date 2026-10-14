package com.cinewave.service;

import com.cinewave.entity.Event;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getEvents(String category, String city, String query) {
        if (query != null && !query.trim().isEmpty()) {
            return eventRepository.findByTitleContainingIgnoreCaseAndActiveTrue(query.trim());
        }
        if (city != null && !city.trim().isEmpty() && !city.equalsIgnoreCase("ALL")) {
            return eventRepository.findByCityIgnoreCaseAndActiveTrue(city.trim());
        }
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            return eventRepository.findByCategoryIgnoreCaseAndActiveTrue(category.trim());
        }
        return eventRepository.findByActiveTrueOrderByEventDateAsc();
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    @Transactional
    public Event create(Event event) {
        if (event.getAvailableTickets() == null) {
            event.setAvailableTickets(event.getTotalTickets());
        }
        return eventRepository.save(event);
    }

    @Transactional
    public Event update(Long id, Event updated) {
        Event existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setVenue(updated.getVenue());
        existing.setCity(updated.getCity());
        existing.setEventDate(updated.getEventDate());
        existing.setEventTime(updated.getEventTime());
        existing.setPrice(updated.getPrice());
        existing.setTotalTickets(updated.getTotalTickets());
        existing.setAvailableTickets(updated.getAvailableTickets());
        existing.setBannerUrl(updated.getBannerUrl());
        existing.setActive(updated.isActive());
        return eventRepository.save(existing);
    }

    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        Event existing = getById(id);
        existing.setActive(false);
        eventRepository.save(existing);
    }

    @Transactional
    public Event toggleStatus(Long id) {
        Event existing = getById(id);
        existing.setActive(!existing.isActive());
        return eventRepository.save(existing);
    }

    @Transactional
    public Event bookTickets(Long id, int quantity) {
        Event event = getById(id);
        if (quantity <= 0) {
            throw new BadRequestException("Ticket quantity must be at least 1");
        }
        if (event.getAvailableTickets() < quantity) {
            throw new BadRequestException("Only " + event.getAvailableTickets() + " tickets left for this event");
        }
        event.setAvailableTickets(event.getAvailableTickets() - quantity);
        return eventRepository.save(event);
    }
}
