package com.cinewave.repository;

import com.cinewave.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheaterId(Long theaterId);

    List<Screen> findByTheaterIdAndActiveTrue(Long theaterId);
}
