package com.cinewave.dto;

public class OtpResponse {
    private boolean success;
    private String message;
    private String maskedDestination;
    private int cooldownSeconds;
    private String deliveryStatus; // "DELIVERED_VIA_EMAIL", "DELIVERED_VIA_SMS", "DEV_SANDBOX_MODE"
    private String configuredNotice;
    private String devOtpHint; // In development/sandbox mode when SMTP/SMS keys not configured

    public OtpResponse() {}

    public OtpResponse(boolean success, String message, String maskedDestination,
                       int cooldownSeconds, String deliveryStatus, String configuredNotice) {
        this.success = success;
        this.message = message;
        this.maskedDestination = maskedDestination;
        this.cooldownSeconds = cooldownSeconds;
        this.deliveryStatus = deliveryStatus;
        this.configuredNotice = configuredNotice;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMaskedDestination() { return maskedDestination; }
    public void setMaskedDestination(String maskedDestination) { this.maskedDestination = maskedDestination; }

    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getConfiguredNotice() { return configuredNotice; }
    public void setConfiguredNotice(String configuredNotice) { this.configuredNotice = configuredNotice; }

    public String getDevOtpHint() { return devOtpHint; }
    public void setDevOtpHint(String devOtpHint) { this.devOtpHint = devOtpHint; }
}
