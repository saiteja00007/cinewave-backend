package com.cinewave.repository;

import com.cinewave.entity.Booking;
import com.cinewave.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    List<Booking> findAllOrderByCreatedAtDesc();

    @Query("SELECT b FROM Booking b WHERE b.bookingReference = :ref OR " +
           "LOWER(b.user.name) LIKE LOWER(CONCAT('%', :ref, '%')) OR " +
           "LOWER(b.user.email) LIKE LOWER(CONCAT('%', :ref, '%'))")
    List<Booking> searchBookings(@Param("ref") String searchTerm);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = com.cinewave.entity.BookingStatus.CONFIRMED")
    java.math.BigDecimal totalRevenue();
}
