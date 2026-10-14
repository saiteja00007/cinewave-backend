package com.cinewave.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class MovieRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    private String description;

    @NotBlank(message = "Genre is required")
    @Size(max = 255)
    private String genre;

    @NotBlank(message = "Language is required")
    @Size(max = 255)
    private String language;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    private LocalDate releaseDate;

    @Size(max = 50)
    private String rating;

    private String castInfo;

    private String posterUrl;

    @Size(max = 500)
    private String trailerUrl;

    private Long tmdbId;

    private String backdropUrl;

    private Double voteAverage;

    private Integer voteCount;

    @Size(max = 100)
    private String director;

    private String tag = "NOW_PLAYING";

    private String originalLanguage = "te";

    private Boolean customizedByAdmin = true;

    public MovieRequest() {}

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

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public Boolean getCustomizedByAdmin() { return customizedByAdmin; }
    public void setCustomizedByAdmin(Boolean customizedByAdmin) { this.customizedByAdmin = customizedByAdmin; }
}
