package com.cinewave.service;

import com.cinewave.dto.*;
import com.cinewave.entity.BookingStatus;
import com.cinewave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public AdminService(MovieRepository movieRepository,
                        TheaterRepository theaterRepository,
                        UserRepository userRepository,
                        ShowRepository showRepository,
                        BookingRepository bookingRepository,
                        BookingSeatRepository bookingSeatRepository) {
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    public DashboardResponse getDashboard() {
        DashboardResponse dash = new DashboardResponse();
        dash.setTotalMovies(movieRepository.countByActiveTrue());
        dash.setTotalTheaters(theaterRepository.countByActiveTrue());
        dash.setTotalUsers(userRepository.count());
        dash.setTotalShows(showRepository.countByActiveTrue());
        dash.setTotalBookings(bookingRepository.count());
        dash.setConfirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        dash.setCancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED));
        dash.setTotalRevenue(bookingRepository.totalRevenue());

        // Recent 10 bookings
        List<BookingResponse> recent = bookingRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .limit(10)
                .map(b -> {
                    b.setBookingSeats(bookingSeatRepository.findByBookingId(b.getId()));
                    return BookingResponse.fromEntity(b);
                })
                .collect(Collectors.toList());
        dash.setRecentBookings(recent);

        return dash;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
