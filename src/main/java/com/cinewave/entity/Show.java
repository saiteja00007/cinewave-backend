package com.cinewave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A scheduled showing of a movie in a screen.
 * Prices per seat category are stored here.
 */
@Entity
@Table(name = "shows", indexes = {
    @Index(name = "idx_show_date", columnList = "show_date"),
    @Index(name = "idx_show_screen", columnList = "screen_id"),
    @Index(name = "idx_show_movie", columnList = "movie_id")
})
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @NotNull
    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Positive
    @Column(name = "price_regular", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceRegular;

    @NotNull
    @Positive
    @Column(name = "price_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePremium;

    @NotNull
    @Positive
    @Column(name = "price_recliner", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceRecliner;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "show", fetch = FetchType.LAZY)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Show() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public Screen getScreen() { return screen; }
    public void setScreen(Screen screen) { this.screen = screen; }

    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public BigDecimal getPriceRegular() { return priceRegular; }
    public void setPriceRegular(BigDecimal priceRegular) { this.priceRegular = priceRegular; }

    public BigDecimal getPricePremium() { return pricePremium; }
    public void setPricePremium(BigDecimal pricePremium) { this.pricePremium = pricePremium; }

    public BigDecimal getPriceRecliner() { return priceRecliner; }
    public void setPriceRecliner(BigDecimal priceRecliner) { this.priceRecliner = priceRecliner; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<BookingSeat> getBookingSeats() { return bookingSeats; }
    public void setBookingSeats(List<BookingSeat> bookingSeats) { this.bookingSeats = bookingSeats; }

    /**
     * Get the price for a specific seat category.
     */
    public BigDecimal getPriceForCategory(SeatCategory category) {
        if (category == SeatCategory.PREMIUM) {
            return pricePremium != null ? pricePremium : priceRegular;
        } else if (category == SeatCategory.RECLINER) {
            return priceRecliner != null ? priceRecliner : priceRegular;
        }
        return priceRegular != null ? priceRegular : BigDecimal.ZERO;
    }
}
