package com.cinewave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ScreenUpdateRequest {

    @NotBlank(message = "Screen name is required")
    @Size(max = 100)
    private String name;

    private Boolean active;

    public ScreenUpdateRequest() {}

    public ScreenUpdateRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
