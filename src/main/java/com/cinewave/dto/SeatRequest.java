package com.cinewave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.cinewave.entity.SeatCategory;

public class SeatRequest {

    @NotBlank(message = "Row label is required")
    @Size(max = 5)
    private String rowLabel;

    @NotNull(message = "Seat number is required")
    private Integer seatNumber;

    @NotNull(message = "Category is required")
    private SeatCategory category;

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    public SeatRequest() {}

    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }
    public SeatCategory getCategory() { return category; }
    public void setCategory(SeatCategory category) { this.category = category; }
    public Long getScreenId() { return screenId; }
    public void setScreenId(Long screenId) { this.screenId = screenId; }
}
