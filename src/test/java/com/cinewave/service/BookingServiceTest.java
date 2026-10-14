package com.cinewave.service;

import com.cinewave.dto.BookingRequest;
import com.cinewave.dto.BookingResponse;
import com.cinewave.dto.SeatResponse;
import com.cinewave.entity.*;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ConflictException;
import com.cinewave.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSeatRepository bookingSeatRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OfferService offerService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BookingService bookingService;

    private User sampleUser;
    private Show sampleShow;
    private Screen sampleScreen;
    private Seat seat1;
    private Seat seat2;

    @BeforeEach
    void setUp() {
        sampleUser = new User("John", "john@example.com", "pass", Role.USER);
        sampleUser.setId(1L);

        sampleScreen = new Screen();
        sampleScreen.setId(5L);
        sampleScreen.setName("Audi 1");

        Theater theater = new Theater();
        theater.setId(10L);
        theater.setName("PVR ICON");
        theater.setCity("Mumbai");
        sampleScreen.setTheater(theater);

        Movie movie = new Movie();
        movie.setId(10L);
        movie.setTitle("Gladiator II");
        movie.setDuration(150);

        sampleShow = new Show();
        sampleShow.setId(20L);
        sampleShow.setMovie(movie);
        sampleShow.setScreen(sampleScreen);
        sampleShow.setShowDate(LocalDate.now().plusDays(1));
        sampleShow.setStartTime(LocalTime.of(18, 0));
        sampleShow.setPriceRegular(BigDecimal.valueOf(200));
        sampleShow.setPricePremium(BigDecimal.valueOf(350));
        sampleShow.setPriceRecliner(BigDecimal.valueOf(500));
        sampleShow.setActive(true);

        seat1 = new Seat("A", 1, SeatCategory.REGULAR, sampleScreen);
        seat1.setId(101L);

        seat2 = new Seat("A", 2, SeatCategory.PREMIUM, sampleScreen);
        seat2.setId(102L);
    }

    @Test
    void createBooking_Success_CalculatesPriceCorrectly() {
        BookingRequest req = new BookingRequest();
        req.setShowId(20L);
        req.setSeatIds(List.of(101L, 102L));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(showRepository.findById(20L)).thenReturn(Optional.of(sampleShow));
        when(seatRepository.findAllById(any())).thenReturn(List.of(seat1, seat2));
        when(bookingSeatRepository.areAnySeatsBooked(eq(20L), any())).thenReturn(false);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(500L);
            return b;
        });
        when(bookingRepository.findById(500L)).thenAnswer(invocation -> {
            Booking b = new Booking();
            b.setId(500L);
            b.setBookingReference("CW12345");
            b.setUser(sampleUser);
            b.setShow(sampleShow);
            // 200 (regular) + 350 (premium) = 550
            b.setTotalAmount(BigDecimal.valueOf(550));
            b.setStatus(BookingStatus.CONFIRMED);
            b.setCreatedAt(LocalDateTime.now());
            return Optional.of(b);
        });

        BookingResponse res = bookingService.createBooking(req, "john@example.com");

        assertNotNull(res);
        assertEquals(500L, res.getId());
        assertEquals("CW12345", res.getBookingReference());
        assertEquals(BigDecimal.valueOf(550), res.getTotalAmount());
        verify(bookingSeatRepository, times(2)).save(any(BookingSeat.class));
    }

    @Test
    void createBooking_SeatsAlreadyBooked_ThrowsConflictException() {
        BookingRequest req = new BookingRequest();
        req.setShowId(20L);
        req.setSeatIds(List.of(101L));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(showRepository.findById(20L)).thenReturn(Optional.of(sampleShow));
        when(seatRepository.findAllById(any())).thenReturn(List.of(seat1));
        when(bookingSeatRepository.areAnySeatsBooked(eq(20L), any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> bookingService.createBooking(req, "john@example.com"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_MoreThan2HoursBefore_Success() {
        Booking booking = new Booking();
        booking.setId(500L);
        booking.setBookingReference("CW12345");
        booking.setUser(sampleUser);
        booking.setShow(sampleShow);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(BigDecimal.valueOf(550));
        booking.setCreatedAt(LocalDateTime.now().minusHours(1));

        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse res = bookingService.cancelBooking(500L, "john@example.com");

        assertNotNull(res);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertNotNull(booking.getCancelledAt());
    }

    @Test
    void cancelBooking_Within2Hours_ThrowsBadRequestException() {
        // Show is only 1 hour away
        sampleShow.setShowDate(LocalDate.now());
        sampleShow.setStartTime(LocalTime.now().plusHours(1));

        Booking booking = new Booking();
        booking.setId(500L);
        booking.setUser(sampleUser);
        booking.setShow(sampleShow);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(500L)).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class, () -> bookingService.cancelBooking(500L, "john@example.com"));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void getShowSeats_ReturnsSeatsWithBookedStatus() {
        when(showRepository.findById(20L)).thenReturn(Optional.of(sampleShow));
        when(bookingSeatRepository.findBookedSeatIdsByShowId(20L)).thenReturn(Set.of(101L));
        when(seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(5L))
                .thenReturn(List.of(seat1, seat2));

        List<SeatResponse> seats = bookingService.getShowSeats(20L);

        assertEquals(2, seats.size());
        assertTrue(seats.get(0).isBooked()); // seat1 (101L) is booked
        assertFalse(seats.get(1).isBooked()); // seat2 (102L) is available
    }
}
