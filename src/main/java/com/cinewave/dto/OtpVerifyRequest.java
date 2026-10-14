package com.cinewave.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpVerifyRequest {

    @NotBlank(message = "Destination is required")
    private String destination;

    private String otp;
    private String otpCode;

    // Optional user registration details if registering during OTP verification
    private String name;
    private String password;

    public OtpVerifyRequest() {}

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getOtp() { return otp != null ? otp : otpCode; }
    public void setOtp(String otp) { 
        this.otp = otp; 
        if (this.otpCode == null) this.otpCode = otp;
    }

    public String getOtpCode() { return getOtp(); }
    public void setOtpCode(String otpCode) { 
        this.otpCode = otpCode; 
        if (this.otp == null) this.otp = otpCode;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
