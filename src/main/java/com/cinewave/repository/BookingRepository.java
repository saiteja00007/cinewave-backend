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

    List<Booking> findByShowIdAndStatus(Long showId, BookingStatus status);

    List<Booking> findByShowShowDateAndStatus(java.time.LocalDate date, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.show.screen.theater.owner.id = :ownerId ORDER BY b.createdAt DESC")
    List<Booking> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.show.screen.theater.id = :theaterId ORDER BY b.createdAt DESC")
    List<Booking> findByTheaterId(@Param("theaterId") Long theaterId);

    @Query("SELECT b FROM Booking b WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(b.user.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(b.user.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(b.show.movie.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(b.show.screen.theater.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:date IS NULL OR b.show.showDate = :date) ORDER BY b.createdAt DESC")
    List<Booking> filterBookings(@Param("query") String query,
                                 @Param("status") BookingStatus status,
                                 @Param("date") java.time.LocalDate date);

    long countByShowMovieIdAndStatus(Long movieId, BookingStatus status);

    long countByShowScreenIdAndStatus(Long screenId, BookingStatus status);

    long countByShowScreenTheaterIdAndStatus(Long theaterId, BookingStatus status);

    long countByShowScreenTheaterIdAndStatusAndShowShowDateGreaterThanEqual(Long theaterId, BookingStatus status, java.time.LocalDate date);

    long countByShowScreenIdAndStatusAndShowShowDateGreaterThanEqual(Long screenId, BookingStatus status, java.time.LocalDate date);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = com.cinewave.entity.BookingStatus.CONFIRMED")
    java.math.BigDecimal totalRevenue();
}
