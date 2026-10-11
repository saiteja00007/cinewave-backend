package com.cinewave.repository;

import com.cinewave.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    /**
     * Find all booked seat IDs for a given show (only CONFIRMED bookings).
     */
    @Query("SELECT bs.seat.id FROM BookingSeat bs WHERE bs.show.id = :showId " +
           "AND bs.booking.status = com.cinewave.entity.BookingStatus.CONFIRMED")
    Set<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);

    /**
     * Check if any of the given seats are already booked for a show.
     */
    @Query("SELECT COUNT(bs) > 0 FROM BookingSeat bs WHERE bs.show.id = :showId " +
           "AND bs.seat.id IN :seatIds AND bs.booking.status = com.cinewave.entity.BookingStatus.CONFIRMED")
    boolean areAnySeatsBooked(@Param("showId") Long showId,
                               @Param("seatIds") List<Long> seatIds);

    List<BookingSeat> findByBookingId(Long bookingId);
}
