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
    private Long tmdbId;
    private String backdropUrl;
    private Double voteAverage;
    private Integer voteCount;
    private String director;
    private boolean active;
    private String tag;
    private String originalLanguage;
    private java.time.LocalDateTime tmdbLastSyncedAt;
    private boolean customizedByAdmin;

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
        dto.setTmdbId(m.getTmdbId());
        dto.setBackdropUrl(m.getBackdropUrl());
        dto.setVoteAverage(m.getVoteAverage());
        dto.setVoteCount(m.getVoteCount());
        dto.setDirector(m.getDirector());
        dto.setActive(m.isActive());
        dto.setTag(m.getTag());
        dto.setOriginalLanguage(m.getOriginalLanguage());
        dto.setTmdbLastSyncedAt(m.getTmdbLastSyncedAt());
        dto.setCustomizedByAdmin(m.isCustomizedByAdmin());
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
    public Integer getReleaseYear() { return releaseDate != null ? releaseDate.getYear() : null; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getCastInfo() { return castInfo; }
    public void setCastInfo(String castInfo) { this.castInfo = castInfo; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }
    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }
    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }
    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }
    public java.time.LocalDateTime getTmdbLastSyncedAt() { return tmdbLastSyncedAt; }
    public void setTmdbLastSyncedAt(java.time.LocalDateTime tmdbLastSyncedAt) { this.tmdbLastSyncedAt = tmdbLastSyncedAt; }
    public boolean isCustomizedByAdmin() { return customizedByAdmin; }
    public void setCustomizedByAdmin(boolean customizedByAdmin) { this.customizedByAdmin = customizedByAdmin; }
}
