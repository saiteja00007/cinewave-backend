package com.cinewave.dto;

import com.cinewave.entity.Show;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ShowResponse {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private Long screenId;
    private String screenName;
    private Long theaterId;
    private String theaterName;
    private String theaterCity;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal priceRegular;
    private BigDecimal pricePremium;
    private BigDecimal priceRecliner;
    private boolean active;

    public static ShowResponse fromEntity(Show s) {
        ShowResponse dto = new ShowResponse();
        dto.setId(s.getId());
        dto.setMovieId(s.getMovie().getId());
        dto.setMovieTitle(s.getMovie().getTitle());
        dto.setMoviePosterUrl(s.getMovie().getPosterUrl());
        dto.setScreenId(s.getScreen().getId());
        dto.setScreenName(s.getScreen().getName());
        dto.setTheaterId(s.getScreen().getTheater().getId());
        dto.setTheaterName(s.getScreen().getTheater().getName());
        dto.setTheaterCity(s.getScreen().getTheater().getCity());
        dto.setShowDate(s.getShowDate());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setPriceRegular(s.getPriceRegular());
        dto.setPricePremium(s.getPricePremium());
        dto.setPriceRecliner(s.getPriceRecliner());
        dto.setActive(s.isActive());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getMoviePosterUrl() { return moviePosterUrl; }
    public void setMoviePosterUrl(String moviePosterUrl) { this.moviePosterUrl = moviePosterUrl; }
    public Long getScreenId() { return screenId; }
    public void setScreenId(Long screenId) { this.screenId = screenId; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getTheaterCity() { return theaterCity; }
    public void setTheaterCity(String theaterCity) { this.theaterCity = theaterCity; }
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
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
