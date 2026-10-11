package com.cinewave.dto;

import com.cinewave.entity.Movie;
import java.time.LocalDate;

public class MovieResponse {

    private Long id;
    private String title;
    private String description;
    private String genre;
    private String language;
    private Integer duration;
    private LocalDate releaseDate;
    private String rating;
    private String castInfo;
    private String posterUrl;
    private String trailerUrl;
    private boolean active;

    public MovieResponse() {}

    public static MovieResponse fromEntity(Movie m) {
        MovieResponse dto = new MovieResponse();
        dto.setId(m.getId());
        dto.setTitle(m.getTitle());
        dto.setDescription(m.getDescription());
        dto.setGenre(m.getGenre());
        dto.setLanguage(m.getLanguage());
        dto.setDuration(m.getDuration());
        dto.setReleaseDate(m.getReleaseDate());
        dto.setRating(m.getRating());
        dto.setCastInfo(m.getCastInfo());
        dto.setPosterUrl(m.getPosterUrl());
        dto.setTrailerUrl(m.getTrailerUrl());
        dto.setActive(m.isActive());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getCastInfo() { return castInfo; }
    public void setCastInfo(String castInfo) { this.castInfo = castInfo; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
