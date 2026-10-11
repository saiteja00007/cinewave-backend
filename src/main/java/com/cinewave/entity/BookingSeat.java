package com.cinewave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Junction table: links a Booking to a specific Seat for a specific Show.
 * 
 * CRITICAL CONSTRAINT: unique(show_id, seat_id) ensures a seat
 * cannot be double-booked for the same show.
 */
@Entity
@Table(name = "booking_seats", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_show_seat",
        columnNames = {"show_id", "seat_id"}
    )
})
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Constructors
    public BookingSeat() {}

    public BookingSeat(Booking booking, Show show, Seat seat, BigDecimal price) {
        this.booking = booking;
        this.show = show;
        this.seat = seat;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Show getShow() { return show; }
    public void setShow(Show show) { this.show = show; }

    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
