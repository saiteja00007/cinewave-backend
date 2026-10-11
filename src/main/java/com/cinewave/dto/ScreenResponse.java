package com.cinewave.dto;

import com.cinewave.entity.Screen;
import java.util.List;
import java.util.stream.Collectors;

public class ScreenResponse {
    private Long id;
    private String name;
    private Long theaterId;
    private String theaterName;
    private boolean active;
    private int seatCount;
    private List<SeatResponse> seats;

    public static ScreenResponse fromEntity(Screen s) {
        ScreenResponse dto = new ScreenResponse();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setTheaterId(s.getTheater().getId());
        dto.setTheaterName(s.getTheater().getName());
        dto.setActive(s.isActive());
        dto.setSeatCount(s.getSeats() != null ? s.getSeats().size() : 0);
        return dto;
    }

    public static ScreenResponse fromEntityWithSeats(Screen s) {
        ScreenResponse dto = fromEntity(s);
        if (s.getSeats() != null) {
            dto.setSeats(s.getSeats().stream()
                .map(SeatResponse::fromEntity)
                .collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }
    public List<SeatResponse> getSeats() { return seats; }
    public void setSeats(List<SeatResponse> seats) { this.seats = seats; }
}
