package com.cinewave.dto;

import com.cinewave.entity.Booking;
import com.cinewave.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long showId;
    private String movieTitle;
    private String moviePosterUrl;
    private String theaterName;
    private String theaterCity;
    private String screenName;
    private LocalDate showDate;
    private LocalTime startTime;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private List<SeatResponse> seats;

    public static BookingResponse fromEntity(Booking b) {
        BookingResponse dto = new BookingResponse();
        dto.setId(b.getId());
        dto.setBookingReference(b.getBookingReference());
        if (b.getUser() != null) {
            dto.setUserId(b.getUser().getId());
            dto.setUserName(b.getUser().getName());
            dto.setUserEmail(b.getUser().getEmail());
        }
        if (b.getShow() != null) {
            dto.setShowId(b.getShow().getId());
            dto.setShowDate(b.getShow().getShowDate());
            dto.setStartTime(b.getShow().getStartTime());
            if (b.getShow().getMovie() != null) {
                dto.setMovieTitle(b.getShow().getMovie().getTitle());
                dto.setMoviePosterUrl(b.getShow().getMovie().getPosterUrl());
            }
            if (b.getShow().getScreen() != null) {
                dto.setScreenName(b.getShow().getScreen().getName());
                if (b.getShow().getScreen().getTheater() != null) {
                    dto.setTheaterName(b.getShow().getScreen().getTheater().getName());
                    dto.setTheaterCity(b.getShow().getScreen().getTheater().getCity());
                }
            }
        }
        dto.setTotalAmount(b.getTotalAmount());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());
        dto.setCancelledAt(b.getCancelledAt());
        if (b.getBookingSeats() != null) {
            dto.setSeats(b.getBookingSeats().stream()
                .map(bs -> SeatResponse.fromEntity(bs.getSeat()))
                .collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public Long getShowId() { return showId; }
    public void setShowId(Long showId) { this.showId = showId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getMoviePosterUrl() { return moviePosterUrl; }
    public void setMoviePosterUrl(String moviePosterUrl) { this.moviePosterUrl = moviePosterUrl; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getTheaterCity() { return theaterCity; }
    public void setTheaterCity(String theaterCity) { this.theaterCity = theaterCity; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public List<SeatResponse> getSeats() { return seats; }
    public void setSeats(List<SeatResponse> seats) { this.seats = seats; }
}
