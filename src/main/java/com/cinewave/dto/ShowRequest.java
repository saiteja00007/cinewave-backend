package com.cinewave.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ShowRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    @NotNull(message = "Show date is required")
    private LocalDate showDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    private LocalTime endTime;

    @NotNull(message = "Regular price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal priceRegular;

    @NotNull(message = "Premium price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal pricePremium;

    @NotNull(message = "Recliner price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal priceRecliner;

    public ShowRequest() {}

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getScreenId() { return screenId; }
    public void setScreenId(Long screenId) { this.screenId = screenId; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public BigDecimal getPriceRegular() { return priceRegular; }
    public void setPriceRegular(BigDecimal priceRegular) { this.priceRegular = priceRegular; }
    public BigDecimal getPricePremium() { return pricePremium; }
    public void setPricePremium(BigDecimal pricePremium) { this.pricePremium = pricePremium; }
    public BigDecimal getPriceRecliner() { return priceRecliner; }
    public void setPriceRecliner(BigDecimal priceRecliner) { this.priceRecliner = priceRecliner; }
}
