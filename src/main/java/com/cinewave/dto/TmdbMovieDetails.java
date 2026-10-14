package com.cinewave.dto;

public class TmdbMovieDetails {
    private Long tmdbId;
    private String title;
    private String originalTitle;
    private String overview;
    private String genre;
    private String language;
    private String originalLanguage;
    private Integer duration;
    private String releaseDate;
    private String rating;
    private String castInfo;
    private String director;
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private Double voteAverage;
    private Integer voteCount;
    private boolean trailerAvailable;
    private String tag = "NOW_PLAYING";

    public TmdbMovieDetails() {}

    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getCastInfo() { return castInfo; }
    public void setCastInfo(String castInfo) { this.castInfo = castInfo; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }

    public boolean isTrailerAvailable() { return trailerAvailable; }
    public void setTrailerAvailable(boolean trailerAvailable) { this.trailerAvailable = trailerAvailable; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}
