package com.cinewave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TheaterRequest {

    @NotBlank(message = "Theater name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 300)
    private String address;

    @Size(max = 500)
    private String facilities;

    public TheaterRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }
}
