package com.cinewave.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpRequest {

    @NotBlank(message = "Destination is required (email or phone)")
    private String destination;

    private String type; // "EMAIL" or "SMS" (auto-detected if omitted)

    public OtpRequest() {}

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
