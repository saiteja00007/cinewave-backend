package com.cinewave.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class MovieRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    private String description;

    @NotBlank(message = "Genre is required")
    @Size(max = 50)
    private String genre;

    @NotBlank(message = "Language is required")
    @Size(max = 50)
    private String language;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    private LocalDate releaseDate;

    @Size(max = 10)
    private String rating;

    @Size(max = 500)
    private String castInfo;

    @Size(max = 500)
    private String posterUrl;

    @Size(max = 500)
    private String trailerUrl;

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
}
