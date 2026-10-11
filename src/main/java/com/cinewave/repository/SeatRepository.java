package com.cinewave.repository;

import com.cinewave.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreenId(Long screenId);

    List<Seat> findByScreenIdOrderByRowLabelAscSeatNumberAsc(Long screenId);
}
