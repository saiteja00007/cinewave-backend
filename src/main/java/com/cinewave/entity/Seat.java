package com.cinewave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Physical seat in a screen.
 * Has a row label (A, B, C...), seat number, and category for pricing.
 * Unique constraint: one seat per (screen, row, seatNumber).
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"screen_id", "row_label", "seat_number"})
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 5)
    @Column(name = "row_label", nullable = false)
    private String rowLabel;

    @NotNull
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatCategory category = SeatCategory.REGULAR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    // Constructors
    public Seat() {}

    public Seat(String rowLabel, Integer seatNumber, SeatCategory category, Screen screen) {
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.category = category;
        this.screen = screen;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public SeatCategory getCategory() { return category; }
    public void setCategory(SeatCategory category) { this.category = category; }

    public Screen getScreen() { return screen; }
    public void setScreen(Screen screen) { this.screen = screen; }
}
