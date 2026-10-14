package com.cinewave.dto;

public class OfferValidationResponse {
    private boolean valid;
    private String code;
    private String title;
    private Double discountAmount;
    private Double finalAmount;
    private String message;

    public OfferValidationResponse() {}

    public OfferValidationResponse(boolean valid, String code, String title, Double discountAmount, Double finalAmount, String message) {
        this.valid = valid;
        this.code = code;
        this.title = title;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.message = message;
    }

    public static OfferValidationResponse failure(String message) {
        return new OfferValidationResponse(false, null, null, 0.0, 0.0, message);
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
