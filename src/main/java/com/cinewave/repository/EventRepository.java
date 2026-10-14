package com.cinewave.repository;

import com.cinewave.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByActiveTrueOrderByEventDateAsc();
    List<Event> findByCategoryIgnoreCaseAndActiveTrue(String category);
    List<Event> findByCityIgnoreCaseAndActiveTrue(String city);
    List<Event> findByTitleContainingIgnoreCaseAndActiveTrue(String query);
}
