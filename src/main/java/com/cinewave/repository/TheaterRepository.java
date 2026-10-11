package com.cinewave.repository;

import com.cinewave.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    List<Theater> findByActiveTrue();

    List<Theater> findByCityAndActiveTrue(String city);

    @Query("SELECT DISTINCT t.city FROM Theater t WHERE t.active = true ORDER BY t.city")
    List<String> findDistinctCities();

    long countByActiveTrue();
}
