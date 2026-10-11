package com.cinewave.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Movie entity with all metadata.
 * Uses soft-delete (active flag) to preserve booking history.
 */
@Entity
@Table(name = "movies", indexes = {
    @Index(name = "idx_movie_genre", columnList = "genre"),
    @Index(name = "idx_movie_language", columnList = "language"),
    @Index(name = "idx_movie_active", columnList = "active")
})
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String genre;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String language;

    @Min(1)
    @Column(nullable = false)
    private Integer duration; // in minutes

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Size(max = 10)
    private String rating; // e.g. "PG-13", "R", "U/A"

    @Size(max = 500)
    @Column(name = "cast_info")
    private String castInfo;

    @Size(max = 500)
    @Column(name = "poster_url")
    private String posterUrl;

    @Size(max = 500)
    @Column(name = "trailer_url")
    private String trailerUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    private List<Show> shows = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Movie() {}

    // Getters and Setters
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Show> getShows() { return shows; }
    public void setShows(List<Show> shows) { this.shows = shows; }
}
