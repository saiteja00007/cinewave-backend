package com.cinewave.dto;

import com.cinewave.entity.SeatCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SeatUpdateRequest {

    @NotBlank(message = "Row label is required")
    @Size(max = 5)
    private String rowLabel;

    @NotNull(message = "Seat number is required")
    private Integer seatNumber;

    @NotNull(message = "Seat category is required")
    private SeatCategory category;

    public SeatUpdateRequest() {}

    public SeatUpdateRequest(String rowLabel, Integer seatNumber, SeatCategory category) {
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.category = category;
    }

    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public SeatCategory getCategory() { return category; }
    public void setCategory(SeatCategory category) { this.category = category; }
}
