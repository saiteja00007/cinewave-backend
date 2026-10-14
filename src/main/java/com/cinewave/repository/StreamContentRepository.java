package com.cinewave.repository;

import com.cinewave.entity.StreamContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreamContentRepository extends JpaRepository<StreamContent, Long> {
    List<StreamContent> findByActiveTrue();
    List<StreamContent> findByCategoryIgnoreCaseAndActiveTrue(String category);
    List<StreamContent> findByTitleContainingIgnoreCaseAndActiveTrue(String query);
}
