package com.cinewave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ScreenRequest {

    @NotBlank(message = "Screen name is required")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Theater ID is required")
    private Long theaterId;

    public ScreenRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
}
