package com.cinewave;

import com.cinewave.dto.*;
import com.cinewave.entity.BookingStatus;
import com.cinewave.repository.MovieRepository;
import com.cinewave.repository.ShowRepository;
import com.cinewave.repository.TheaterRepository;
import com.cinewave.repository.UserRepository;
import com.cinewave.service.AuthService;
import com.cinewave.service.BookingService;
import com.cinewave.service.MovieService;
import com.cinewave.service.ShowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CineWaveIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowService showService;

    @Autowired
    private BookingService bookingService;

    @Test
    void contextLoads_AndDataSeedingPopulatesCatalog() {
        // DataSeeder should have seeded movies, theaters, and shows
        assertTrue(movieRepository.count() > 0, "Movies should be seeded");
        assertTrue(theaterRepository.count() > 0, "Theaters should be seeded");
        assertTrue(showRepository.count() > 0, "Shows should be seeded");
    }

    @Test
    void endToEnd_UserRegistration_MovieBrowsing_AndBookingFlow() {
        // 1. User Registration
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setName("Test Patron");
        registerReq.setEmail("patron" + System.currentTimeMillis() + "@test.com");
        registerReq.setPassword("Password@123");

        AuthResponse authRes = authService.register(registerReq);
        assertNotNull(authRes.getToken());
        assertEquals(registerReq.getEmail(), authRes.getUser().getEmail());

        // 2. Browse Movies
        List<MovieResponse> movies = movieService.getAllActiveMovies();
        assertFalse(movies.isEmpty(), "Should find active movies");
        Long movieId = movies.get(0).getId();

        // 3. Find upcoming shows for the movie
        List<ShowResponse> shows = showService.getShowsByMovie(movieId);
        assertFalse(shows.isEmpty(), "Should find shows for seeded movie");
        ShowResponse futureShow = shows.stream()
                .filter(s -> java.time.LocalDateTime.of(s.getShowDate(), s.getStartTime())
                        .isAfter(java.time.LocalDateTime.now()))
                .findFirst().orElseThrow(() -> new IllegalStateException("No future shows found"));
        Long showId = futureShow.getId();

        // 4. View Seat Layout
        List<SeatResponse> seats = bookingService.getShowSeats(showId);
        assertFalse(seats.isEmpty(), "Should have seats for the show");

        // Pick 2 available seats
        SeatResponse seat1 = seats.get(0);
        SeatResponse seat2 = seats.get(1);
        assertFalse(seat1.isBooked(), "Seat should be initially available");
        assertFalse(seat2.isBooked(), "Seat should be initially available");

        // 5. Book Selected Seats
        BookingRequest bookingReq = new BookingRequest();
        bookingReq.setShowId(showId);
        bookingReq.setSeatIds(List.of(seat1.getId(), seat2.getId()));

        BookingResponse bookingRes = bookingService.createBooking(bookingReq, registerReq.getEmail());
        assertNotNull(bookingRes);
        assertNotNull(bookingRes.getBookingReference());
        assertEquals(BookingStatus.CONFIRMED, bookingRes.getStatus());
        assertTrue(bookingRes.getTotalAmount().doubleValue() > 0);

        // 6. Verify Seats are Now Booked
        List<SeatResponse> updatedSeats = bookingService.getShowSeats(showId);
        SeatResponse recheckedSeat1 = updatedSeats.stream()
                .filter(s -> s.getId().equals(seat1.getId()))
                .findFirst().orElseThrow();
        assertTrue(recheckedSeat1.isBooked(), "Seat 1 should now be booked");

        // 7. Verify User Booking History
        List<BookingResponse> myBookings = bookingService.getUserBookings(registerReq.getEmail());
        assertFalse(myBookings.isEmpty());
        assertEquals(bookingRes.getBookingReference(), myBookings.get(0).getBookingReference());
    }
}
