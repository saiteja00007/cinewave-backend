package com.cinewave.dto;

import com.cinewave.entity.Theater;

public class TheaterResponse {
    private Long id;
    private String name;
    private String city;
    private String address;
    private String facilities;
    private boolean active;
    private int screenCount;

    public static TheaterResponse fromEntity(Theater t) {
        TheaterResponse dto = new TheaterResponse();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setCity(t.getCity());
        dto.setAddress(t.getAddress());
        dto.setFacilities(t.getFacilities());
        dto.setActive(t.isActive());
        dto.setScreenCount(t.getScreens() != null ? t.getScreens().size() : 0);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getScreenCount() { return screenCount; }
    public void setScreenCount(int screenCount) { this.screenCount = screenCount; }
}
