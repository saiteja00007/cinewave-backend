package com.cinewave.dto;

public class TmdbSearchResult {
    private Long id;
    private String title;
    private String originalTitle;
    private String releaseDate;
    private String releaseYear;
    private String posterUrl;
    private String backdropUrl;
    private String overview;
    private Double voteAverage;

    public TmdbSearchResult() {}

    public TmdbSearchResult(Long id, String title, String originalTitle, String releaseDate,
                            String releaseYear, String posterUrl, String backdropUrl,
                            String overview, Double voteAverage) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.releaseYear = releaseYear;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.overview = overview;
        this.voteAverage = voteAverage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTmdbId() { return id; }
    public void setTmdbId(Long tmdbId) { this.id = tmdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getReleaseYear() { return releaseYear; }
    public void setReleaseYear(String releaseYear) { this.releaseYear = releaseYear; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }
}
