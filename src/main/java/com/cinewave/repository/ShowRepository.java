package com.cinewave.repository;

import com.cinewave.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovieIdAndActiveTrue(Long movieId);

    List<Show> findByScreenIdAndShowDateAndActiveTrue(Long screenId, LocalDate showDate);

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId AND s.active = true " +
           "AND s.showDate >= :today ORDER BY s.showDate, s.startTime")
    List<Show> findUpcomingShowsByMovie(@Param("movieId") Long movieId,
                                        @Param("today") LocalDate today);

    @Query("SELECT s FROM Show s WHERE s.screen.id = :screenId AND s.showDate = :date " +
           "AND s.active = true " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Show> findOverlappingShows(@Param("screenId") Long screenId,
                                    @Param("date") LocalDate date,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("endTime") LocalTime endTime);

    @Query("SELECT s FROM Show s WHERE s.active = true " +
           "AND s.showDate >= :today ORDER BY s.showDate, s.startTime")
    List<Show> findAllUpcoming(@Param("today") LocalDate today);

    long countByActiveTrue();
}
