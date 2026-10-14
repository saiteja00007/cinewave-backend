package com.cinewave.service;

import com.cinewave.dto.TmdbMovieDetails;
import com.cinewave.dto.TmdbSearchResult;
import com.cinewave.entity.Movie;
import com.cinewave.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service to automatically synchronize live movie records from TMDB (India Region) into MySQL.
 * Prevents duplicate records using TMDB IDs.
 * Preserves admin customizations and existing bookings.
 */
@Service
public class MovieSyncService {

    private static final Logger log = LoggerFactory.getLogger(MovieSyncService.class);

    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);
    private LocalDateTime lastSyncTime;
    private String lastSyncStatus = "IDLE";
    private int lastSyncedCount = 0;

    public MovieSyncService(MovieRepository movieRepository, TmdbService tmdbService) {
        this.movieRepository = movieRepository;
        this.tmdbService = tmdbService;
    }

    /**
     * Initial movie sync on startup if database has few or missing movies.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        long movieCount = movieRepository.countByActiveTrue();
        log.info("CineWave startup: {} active movies currently in database", movieCount);
        if (movieCount < 8) {
            log.info("Seeding initial live Indian & Global theatrical movies from TMDB/curated catalog...");
            syncMoviesFromTmdb(false);
        }
    }

    /**
     * Scheduled synchronization: Daily at 4:00 AM.
     */
    @Scheduled(cron = "${cinewave.sync.movies.cron:0 0 4 * * ?}")
    public void scheduledSync() {
        log.info("Running scheduled TMDB movie synchronization (India Region)...");
        syncMoviesFromTmdb(false);
    }

    /**
     * Synchronize movies from TMDB or curated Indian catalog.
     * Can be invoked on-demand by Admin.
     */
    @Transactional
    public synchronized Map<String, Object> syncMoviesFromTmdb(boolean force) {
        Map<String, Object> result = new HashMap<>();
        if (!isSyncing.compareAndSet(false, true)) {
            result.put("status", "ALREADY_RUNNING");
            result.put("message", "A movie synchronization job is already running.");
            return result;
        }

        int added = 0;
        int updated = 0;
        int skipped = 0;

        try {
            log.info("Starting TMDB movie synchronization (India Region IN)...");
            lastSyncStatus = "IN_PROGRESS";

            Set<Long> processedTmdbIds = new HashSet<>();

            // 1. Now Playing in India
            List<TmdbSearchResult> nowPlaying = tmdbService.getNowPlaying(1);
            for (TmdbSearchResult item : nowPlaying) {
                if (item.getId() != null && processedTmdbIds.add(item.getId())) {
                    int status = upsertMovie(item.getId(), "NOW_PLAYING", null);
                    if (status == 1) added++; else if (status == 2) updated++; else skipped++;
                }
            }

            // 2. Upcoming in India
            List<TmdbSearchResult> upcoming = tmdbService.getUpcoming(1);
            for (TmdbSearchResult item : upcoming) {
                if (item.getId() != null && processedTmdbIds.add(item.getId())) {
                    int status = upsertMovie(item.getId(), "UPCOMING", null);
                    if (status == 1) added++; else if (status == 2) updated++; else skipped++;
                }
            }

            // 3. Popular in India
            List<TmdbSearchResult> popular = tmdbService.getPopular(1);
            for (TmdbSearchResult item : popular) {
                if (item.getId() != null && processedTmdbIds.add(item.getId())) {
                    int status = upsertMovie(item.getId(), "POPULAR", null);
                    if (status == 1) added++; else if (status == 2) updated++; else skipped++;
                }
            }

            // 4. Regional Languages: Telugu, Hindi, Tamil, Malayalam, Kannada, English
            String[] regionalLanguages = {"te", "hi", "ta", "ml", "kn", "en"};
            for (String lang : regionalLanguages) {
                List<TmdbSearchResult> langResults = tmdbService.discoverByLanguage(lang, 1);
                for (TmdbSearchResult item : langResults) {
                    if (item.getId() != null && processedTmdbIds.add(item.getId())) {
                        int status = upsertMovie(item.getId(), "NOW_PLAYING", lang);
                        if (status == 1) added++; else if (status == 2) updated++; else skipped++;
                    }
                }
            }

            lastSyncTime = LocalDateTime.now();
            lastSyncedCount = added + updated;
            lastSyncStatus = "SUCCESS";

            result.put("status", "SUCCESS");
            result.put("addedCount", added);
            result.put("updatedCount", updated);
            result.put("skippedCount", skipped);
            result.put("totalProcessed", processedTmdbIds.size());
            result.put("syncedAt", lastSyncTime.toString());
            log.info("Movie synchronization completed: {} added, {} updated, {} skipped", added, updated, skipped);

        } catch (Exception e) {
            log.error("Movie synchronization failed: {}", e.getMessage(), e);
            lastSyncStatus = "FAILED";
            result.put("status", "FAILED");
            result.put("message", e.getMessage());
        } finally {
            isSyncing.set(false);
        }

        return result;
    }

    /**
     * Upsert a movie by tmdbId.
     * Return 1 if added, 2 if updated, 0 if skipped.
     */
    private int upsertMovie(Long tmdbId, String tag, String originalLanguageOverride) {
        try {
            Optional<Movie> existingOpt = movieRepository.findByTmdbId(tmdbId);
            TmdbMovieDetails details = tmdbService.getMovieDetails(tmdbId);
            if (details == null || !StringUtils.hasText(details.getTitle())) {
                return 0;
            }

            if (existingOpt.isPresent()) {
                Movie movie = existingOpt.get();
                // If customized by admin, do NOT overwrite admin's title, description, or poster!
                if (movie.isCustomizedByAdmin()) {
                    movie.setVoteAverage(details.getVoteAverage());
                    movie.setVoteCount(details.getVoteCount());
                    movie.setTmdbLastSyncedAt(LocalDateTime.now());
                    movieRepository.save(movie);
                    return 2;
                }

                // Update metadata from TMDB
                movie.setTitle(details.getTitle());
                if (StringUtils.hasText(details.getOverview())) {
                    movie.setDescription(details.getOverview());
                }
                if (StringUtils.hasText(details.getGenre())) {
                    movie.setGenre(details.getGenre());
                }
                if (StringUtils.hasText(details.getLanguage())) {
                    movie.setLanguage(details.getLanguage());
                }
                if (StringUtils.hasText(details.getOriginalLanguage())) {
                    movie.setOriginalLanguage(details.getOriginalLanguage());
                }
                if (details.getDuration() != null && details.getDuration() > 0) {
                    movie.setDuration(details.getDuration());
                }
                if (StringUtils.hasText(details.getRating())) {
                    movie.setRating(details.getRating());
                }
                if (StringUtils.hasText(details.getCastInfo())) {
                    movie.setCastInfo(details.getCastInfo());
                }
                if (StringUtils.hasText(details.getDirector())) {
                    movie.setDirector(details.getDirector());
                }
                if (StringUtils.hasText(details.getPosterUrl())) {
                    movie.setPosterUrl(details.getPosterUrl());
                }
                if (StringUtils.hasText(details.getBackdropUrl())) {
                    movie.setBackdropUrl(details.getBackdropUrl());
                }
                if (StringUtils.hasText(details.getTrailerUrl())) {
                    movie.setTrailerUrl(details.getTrailerUrl());
                }
                if (details.getVoteAverage() != null) {
                    movie.setVoteAverage(details.getVoteAverage());
                }
                if (details.getVoteCount() != null) {
                    movie.setVoteCount(details.getVoteCount());
                }
                if (tag != null && !StringUtils.hasText(movie.getTag())) {
                    movie.setTag(tag);
                }
                movie.setTmdbLastSyncedAt(LocalDateTime.now());
                movieRepository.save(movie);
                return 2;

            } else {
                // Insert new movie
                Movie movie = new Movie();
                movie.setTmdbId(tmdbId);
                movie.setTitle(details.getTitle());
                movie.setDescription(StringUtils.hasText(details.getOverview()) ? details.getOverview() : "A cinematic masterpiece.");
                movie.setGenre(StringUtils.hasText(details.getGenre()) ? details.getGenre() : "Action / Drama");
                movie.setLanguage(StringUtils.hasText(details.getLanguage()) ? details.getLanguage() : "Telugu");
                movie.setOriginalLanguage(StringUtils.hasText(details.getOriginalLanguage())
                        ? details.getOriginalLanguage()
                        : (originalLanguageOverride != null ? originalLanguageOverride : "te"));
                movie.setDuration(details.getDuration() != null && details.getDuration() > 0 ? details.getDuration() : 145);
                movie.setReleaseDate(parseDate(details.getReleaseDate()));
                movie.setRating(StringUtils.hasText(details.getRating()) ? details.getRating() : "UA 16+");
                movie.setCastInfo(details.getCastInfo());
                movie.setDirector(details.getDirector());
                movie.setPosterUrl(details.getPosterUrl());
                movie.setBackdropUrl(details.getBackdropUrl());
                movie.setTrailerUrl(details.getTrailerUrl());
                movie.setVoteAverage(details.getVoteAverage() != null ? details.getVoteAverage() : 8.0);
                movie.setVoteCount(details.getVoteCount() != null ? details.getVoteCount() : 1000);
                movie.setTag(StringUtils.hasText(tag) ? tag : (details.getTag() != null ? details.getTag() : "NOW_PLAYING"));
                movie.setActive(true);
                movie.setCustomizedByAdmin(false);
                movie.setTmdbLastSyncedAt(LocalDateTime.now());

                movieRepository.save(movie);
                return 1;
            }
        } catch (Exception e) {
            log.warn("Error upserting movie with tmdbId {}: {}", tmdbId, e.getMessage());
            return 0;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return LocalDate.now();
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isSyncing", isSyncing.get());
        status.put("lastSyncStatus", lastSyncStatus);
        status.put("lastSyncTime", lastSyncTime != null ? lastSyncTime.toString() : null);
        status.put("lastSyncedCount", lastSyncedCount);
        status.put("tmdbApiConfigured", tmdbService.isConfigured());
        status.put("totalMoviesInDb", movieRepository.countByActiveTrue());
        return status;
    }
}
