package com.cinewave.dto;

import com.cinewave.entity.Seat;
import com.cinewave.entity.SeatCategory;

public class SeatResponse {
    private Long id;
    private String rowLabel;
    private Integer seatNumber;
    private SeatCategory category;
    private boolean booked; // only used when returning seat map for a show

    public static SeatResponse fromEntity(Seat s) {
        SeatResponse dto = new SeatResponse();
        dto.setId(s.getId());
        dto.setRowLabel(s.getRowLabel());
        dto.setSeatNumber(s.getSeatNumber());
        dto.setCategory(s.getCategory());
        dto.setBooked(false);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }
    public SeatCategory getCategory() { return category; }
    public void setCategory(SeatCategory category) { this.category = category; }
    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }
}
