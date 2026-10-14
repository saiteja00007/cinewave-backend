package com.cinewave.service;

import com.cinewave.dto.TmdbMovieDetails;
import com.cinewave.dto.TmdbSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for communicating with The Movie Database (TMDB) API.
 * Uses region=IN for theatrical releases in India.
 * Provides live search, now playing, upcoming, popular, and language discovery,
 * plus full trailer, cast/crew, censor ratings, and high-def poster/backdrop resolution.
 *
 * If TMDB_API_KEY is not configured or TMDB is unreachable,
 * a built-in curated Indian/Global blockbusters catalog is provided as an automatic fallback.
 */
@Service
@SuppressWarnings("null")
public class TmdbService {

    private static final Logger log = LoggerFactory.getLogger(TmdbService.class);

    @Value("${cinewave.tmdb.api-key:}")
    private String apiKey;

    @Value("${cinewave.tmdb.base-url:https://api.themoviedb.org/3}")
    private String baseUrl;

    @Value("${cinewave.tmdb.image-base-url:https://image.tmdb.org/t/p}")
    private String imageBaseUrl;

    private final RestTemplate restTemplate;

    public TmdbService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(7))
                .build();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey) && !apiKey.trim().equalsIgnoreCase("none");
    }

    /**
     * Search movies by title.
     */
    @SuppressWarnings("unchecked")
    public List<TmdbSearchResult> searchMovies(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        if (isConfigured()) {
            try {
                String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
                String url = String.format("%s/search/movie?api_key=%s&query=%s&include_adult=false&region=IN&page=1",
                        baseUrl, apiKey.trim(), encodedQuery);

                log.info("Querying TMDB API for search: {}", query.trim());
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response != null && response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    return results.stream()
                            .limit(15)
                            .map(this::mapToSearchResult)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("TMDB search failed ({}), falling back to internal catalog presets: {}", e.getMessage(), query);
            }
        } else {
            log.info("TMDB_API_KEY not configured. Using built-in movie catalog for query: '{}'", query);
        }

        return searchOfflineCatalog(query);
    }

    /**
     * Now Playing movies in theaters in India (region=IN).
     */
    @SuppressWarnings("unchecked")
    public List<TmdbSearchResult> getNowPlaying(int page) {
        int targetPage = Math.max(page, 1);
        if (isConfigured()) {
            try {
                String url = String.format("%s/movie/now_playing?api_key=%s&region=IN&page=%d",
                        baseUrl, apiKey.trim(), targetPage);
                log.info("Fetching TMDB Now Playing movies for India region (page {})", targetPage);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    return results.stream()
                            .map(this::mapToSearchResult)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("TMDB getNowPlaying failed ({}), falling back to offline catalog", e.getMessage());
            }
        }

        return OFFLINE_CATALOG.stream()
                .filter(m -> "NOW_PLAYING".equalsIgnoreCase(m.getTag()))
                .map(this::mapDetailsToSearchResult)
                .collect(Collectors.toList());
    }

    /**
     * Upcoming theatrical releases in India (region=IN).
     */
    @SuppressWarnings("unchecked")
    public List<TmdbSearchResult> getUpcoming(int page) {
        int targetPage = Math.max(page, 1);
        if (isConfigured()) {
            try {
                String url = String.format("%s/movie/upcoming?api_key=%s&region=IN&page=%d",
                        baseUrl, apiKey.trim(), targetPage);
                log.info("Fetching TMDB Upcoming movies for India region (page {})", targetPage);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    return results.stream()
                            .map(this::mapToSearchResult)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("TMDB getUpcoming failed ({}), falling back to offline catalog", e.getMessage());
            }
        }

        return OFFLINE_CATALOG.stream()
                .filter(m -> "UPCOMING".equalsIgnoreCase(m.getTag()))
                .map(this::mapDetailsToSearchResult)
                .collect(Collectors.toList());
    }

    /**
     * Popular movies in India (region=IN).
     */
    @SuppressWarnings("unchecked")
    public List<TmdbSearchResult> getPopular(int page) {
        int targetPage = Math.max(page, 1);
        if (isConfigured()) {
            try {
                String url = String.format("%s/movie/popular?api_key=%s&region=IN&page=%d",
                        baseUrl, apiKey.trim(), targetPage);
                log.info("Fetching TMDB Popular movies for India region (page {})", targetPage);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    return results.stream()
                            .map(this::mapToSearchResult)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("TMDB getPopular failed ({}), falling back to offline catalog", e.getMessage());
            }
        }

        return OFFLINE_CATALOG.stream()
                .sorted((a, b) -> Double.compare(
                        b.getVoteAverage() != null ? b.getVoteAverage() : 0.0,
                        a.getVoteAverage() != null ? a.getVoteAverage() : 0.0))
                .map(this::mapDetailsToSearchResult)
                .collect(Collectors.toList());
    }

    /**
     * Discover movies by language with India region release.
     * Language codes: te (Telugu), hi (Hindi), ta (Tamil), ml (Malayalam), kn (Kannada), en (English)
     */
    @SuppressWarnings("unchecked")
    public List<TmdbSearchResult> discoverByLanguage(String langCode, int page) {
        int targetPage = Math.max(page, 1);
        String code = StringUtils.hasText(langCode) ? langCode.toLowerCase().trim() : "te";

        if (isConfigured()) {
            try {
                String url = String.format("%s/discover/movie?api_key=%s&region=IN&with_original_language=%s&sort_by=popularity.desc&page=%d",
                        baseUrl, apiKey.trim(), code, targetPage);
                log.info("Discovering TMDB movies for language '{}' in India region (page {})", code, targetPage);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    return results.stream()
                            .map(this::mapToSearchResult)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("TMDB discoverByLanguage failed for '{}' ({}), falling back to offline catalog", code, e.getMessage());
            }
        }

        return OFFLINE_CATALOG.stream()
                .filter(m -> code.equalsIgnoreCase(m.getOriginalLanguage()) ||
                             (m.getLanguage() != null && m.getLanguage().toLowerCase().contains(mapLanguageName(code).toLowerCase())))
                .map(this::mapDetailsToSearchResult)
                .collect(Collectors.toList());
    }

    /**
     * Fetch complete details for a movie from TMDB, including cast, director,
     * YouTube trailer, censor rating, duration, and backdrops.
     */
    @SuppressWarnings("unchecked")
    public TmdbMovieDetails getMovieDetails(Long tmdbId) {
        if (isConfigured()) {
            try {
                String url = String.format("%s/movie/%d?api_key=%s&append_to_response=videos,credits,release_dates",
                        baseUrl, tmdbId, apiKey.trim());

                log.info("Querying TMDB movie details for ID: {}", tmdbId);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response != null) {
                    return parseMovieDetails(response, tmdbId);
                }
            } catch (Exception e) {
                log.warn("TMDB details fetch failed for ID {}: {}", tmdbId, e.getMessage());
            }
        }

        return getOfflineMovieDetails(tmdbId);
    }

    public List<TmdbMovieDetails> getAllOfflineCatalog() {
        return Collections.unmodifiableList(OFFLINE_CATALOG);
    }

    private TmdbSearchResult mapToSearchResult(Map<String, Object> item) {
        Long id = ((Number) item.get("id")).longValue();
        String title = (String) item.get("title");
        String originalTitle = (String) item.get("original_title");
        String releaseDate = (String) item.get("release_date");
        String releaseYear = "";
        if (StringUtils.hasText(releaseDate) && releaseDate.length() >= 4) {
            releaseYear = releaseDate.substring(0, 4);
        }
        String posterPath = (String) item.get("poster_path");
        String backdropPath = (String) item.get("backdrop_path");
        String posterUrl = formatImageUrl(posterPath, "w500");
        String backdropUrl = formatImageUrl(backdropPath, "original");
        String overview = (String) item.get("overview");
        Double voteAverage = item.get("vote_average") != null ? ((Number) item.get("vote_average")).doubleValue() : null;

        return new TmdbSearchResult(id, title, originalTitle, releaseDate, releaseYear,
                posterUrl, backdropUrl, overview, voteAverage);
    }

    private TmdbSearchResult mapDetailsToSearchResult(TmdbMovieDetails m) {
        String year = "";
        if (StringUtils.hasText(m.getReleaseDate()) && m.getReleaseDate().length() >= 4) {
            year = m.getReleaseDate().substring(0, 4);
        }
        return new TmdbSearchResult(
                m.getTmdbId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getReleaseDate(),
                year,
                m.getPosterUrl(),
                m.getBackdropUrl(),
                m.getOverview(),
                m.getVoteAverage());
    }

    public String formatImageUrl(String path, String size) {
        if (!StringUtils.hasText(path)) return "";
        path = path.trim();
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("data:")) {
            if (path.contains("https://image.tmdb.org/t/p/" + size + "/https://image.tmdb.org/t/p/" + size + "/")) {
                path = path.replace("https://image.tmdb.org/t/p/" + size + "/https://image.tmdb.org/t/p/" + size + "/", "https://image.tmdb.org/t/p/" + size + "/");
            }
            return path;
        }
        return imageBaseUrl + "/" + size + (path.startsWith("/") ? path : "/" + path);
    }

    @SuppressWarnings("unchecked")
    private TmdbMovieDetails parseMovieDetails(Map<String, Object> response, Long tmdbId) {
        TmdbMovieDetails details = new TmdbMovieDetails();
        details.setTmdbId(tmdbId);
        details.setTitle((String) response.get("title"));
        details.setOriginalTitle((String) response.get("original_title"));
        details.setOverview((String) response.get("overview"));

        // Runtime / duration
        if (response.get("runtime") != null) {
            details.setDuration(((Number) response.get("runtime")).intValue());
        } else {
            details.setDuration(135);
        }

        // Release date
        String releaseDate = (String) response.get("release_date");
        details.setReleaseDate(releaseDate);

        // Language
        String origLang = (String) response.get("original_language");
        details.setOriginalLanguage(origLang != null ? origLang : "te");
        details.setLanguage(mapLanguageCode(origLang));

        // Poster and Backdrop
        String posterPath = (String) response.get("poster_path");
        if (StringUtils.hasText(posterPath)) {
            details.setPosterUrl(formatImageUrl(posterPath, "w500"));
        }
        String backdropPath = (String) response.get("backdrop_path");
        if (StringUtils.hasText(backdropPath)) {
            details.setBackdropUrl(formatImageUrl(backdropPath, "original"));
        }

        // Genres
        if (response.containsKey("genres")) {
            List<Map<String, Object>> genres = (List<Map<String, Object>>) response.get("genres");
            String genreStr = genres.stream()
                    .map(g -> (String) g.get("name"))
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(" / "));
            details.setGenre(genreStr);
        }

        // Vote average and count
        if (response.get("vote_average") != null) {
            details.setVoteAverage(((Number) response.get("vote_average")).doubleValue());
        }
        if (response.get("vote_count") != null) {
            details.setVoteCount(((Number) response.get("vote_count")).intValue());
        }

        // Trailer from videos
        details.setTrailerAvailable(false);
        if (response.containsKey("videos")) {
            Map<String, Object> videosMap = (Map<String, Object>) response.get("videos");
            if (videosMap != null && videosMap.containsKey("results")) {
                List<Map<String, Object>> videos = (List<Map<String, Object>>) videosMap.get("results");
                for (Map<String, Object> vid : videos) {
                    String site = (String) vid.get("site");
                    String type = (String) vid.get("type");
                    String key = (String) vid.get("key");
                    if ("YouTube".equalsIgnoreCase(site) && ("Trailer".equalsIgnoreCase(type) || "Teaser".equalsIgnoreCase(type))) {
                        details.setTrailerUrl("https://www.youtube.com/watch?v=" + key);
                        details.setTrailerAvailable(true);
                        break;
                    }
                }
            }
        }

        // Cast & Crew from credits
        if (response.containsKey("credits")) {
            Map<String, Object> credits = (Map<String, Object>) response.get("credits");
            if (credits != null) {
                if (credits.containsKey("cast")) {
                    List<Map<String, Object>> castList = (List<Map<String, Object>>) credits.get("cast");
                    String castNames = castList.stream()
                            .limit(5)
                            .map(c -> (String) c.get("name"))
                            .filter(StringUtils::hasText)
                            .collect(Collectors.joining(", "));
                    details.setCastInfo(castNames);
                }
                if (credits.containsKey("crew")) {
                    List<Map<String, Object>> crewList = (List<Map<String, Object>>) credits.get("crew");
                    for (Map<String, Object> member : crewList) {
                        if ("Director".equalsIgnoreCase((String) member.get("job"))) {
                            details.setDirector((String) member.get("name"));
                            break;
                        }
                    }
                }
            }
        }

        // Rating from release_dates (Censor certification in India, fallback to US)
        details.setRating("UA 16+");
        if (response.containsKey("release_dates")) {
            Map<String, Object> rd = (Map<String, Object>) response.get("release_dates");
            if (rd != null && rd.containsKey("results")) {
                List<Map<String, Object>> countries = (List<Map<String, Object>>) rd.get("results");
                for (Map<String, Object> country : countries) {
                    String code = (String) country.get("iso_3166_1");
                    if ("IN".equalsIgnoreCase(code)) {
                        List<Map<String, Object>> releaseDateList = (List<Map<String, Object>>) country.get("release_dates");
                        if (releaseDateList != null) {
                            for (Map<String, Object> item : releaseDateList) {
                                String cert = (String) item.get("certification");
                                if (StringUtils.hasText(cert)) {
                                    details.setRating(cert);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return details;
    }

    private String mapLanguageCode(String code) {
        if (code == null) return "Telugu";
        switch (code.toLowerCase()) {
            case "te": return "Telugu";
            case "hi": return "Hindi";
            case "ta": return "Tamil";
            case "ml": return "Malayalam";
            case "kn": return "Kannada";
            case "ja": return "Japanese";
            case "ko": return "Korean";
            case "es": return "Spanish";
            case "fr": return "French";
            default: return "English";
        }
    }

    private String mapLanguageName(String code) {
        return mapLanguageCode(code);
    }

    // --- Curated Authentic Indian & Global Theatrical Catalog Fallback ---
    private static final List<TmdbMovieDetails> OFFLINE_CATALOG = new ArrayList<>();

    static {
        // --- 1. TELUGU ---
        // Kalki 2898 AD (801688)
        TmdbMovieDetails kalki = new TmdbMovieDetails();
        kalki.setTmdbId(801688L);
        kalki.setTitle("Kalki 2898 AD");
        kalki.setOriginalTitle("Kalki 2898 AD");
        kalki.setOverview("Set in a post-apocalyptic world in the year 2898 AD, a modern-day avatar of Vishnu descends to protect the world from evil forces.");
        kalki.setGenre("Sci-Fi / Action / Mythological");
        kalki.setLanguage("Telugu, Hindi, Tamil");
        kalki.setOriginalLanguage("te");
        kalki.setDuration(181);
        kalki.setReleaseDate("2024-06-27");
        kalki.setRating("UA 13+");
        kalki.setCastInfo("Prabhas, Amitabh Bachchan, Kamal Haasan, Deepika Padukone");
        kalki.setDirector("Nag Ashwin");
        kalki.setPosterUrl("https://image.tmdb.org/t/p/w500/o8XSR1SONnjcsv84NRu6Mwsl5io.jpg");
        kalki.setBackdropUrl("https://image.tmdb.org/t/p/original/2KGxQFV9Wp1MshPBf8BuqW0VAu3.jpg");
        kalki.setTrailerUrl("https://www.youtube.com/watch?v=kQDd1AhGIHk");
        kalki.setVoteAverage(8.5);
        kalki.setVoteCount(31000);
        kalki.setTrailerAvailable(true);
        kalki.setTag("NOW_PLAYING");
        OFFLINE_CATALOG.add(kalki);

        // Pushpa 2: The Rule (857598)
        TmdbMovieDetails pushpa2 = new TmdbMovieDetails();
        pushpa2.setTmdbId(857598L);
        pushpa2.setTitle("Pushpa 2: The Rule");
        pushpa2.setOriginalTitle("Pushpa: The Rule - Part 2");
        pushpa2.setOverview("Pushpa Raj rises from a coolie to the syndicate boss of red sandalwood smuggling, clashing with SP Bhanwar Singh Shekhawat in an explosive confrontation of supremacy.");
        pushpa2.setGenre("Action / Crime / Drama");
        pushpa2.setLanguage("Telugu, Hindi, Tamil");
        pushpa2.setOriginalLanguage("te");
        pushpa2.setDuration(195);
        pushpa2.setReleaseDate("2024-12-05");
        pushpa2.setRating("UA 16+");
        pushpa2.setCastInfo("Allu Arjun, Rashmika Mandanna, Fahadh Faasil, Sunil");
        pushpa2.setDirector("Sukumar");
        pushpa2.setPosterUrl("https://image.tmdb.org/t/p/w500/7jGItf7idJsBm9QTNoTNTU3KiGe.jpg");
        pushpa2.setBackdropUrl("https://image.tmdb.org/t/p/original/b33nnKl1GSFbao8l3urDDgr4YxF.jpg");
        pushpa2.setTrailerUrl("https://www.youtube.com/watch?v=1kVK0MZlbI4");
        pushpa2.setVoteAverage(8.8);
        pushpa2.setVoteCount(24500);
        pushpa2.setTrailerAvailable(true);
        pushpa2.setTag("NOW_PLAYING");
        OFFLINE_CATALOG.add(pushpa2);

        // Devara: Part 1 (842675)
        TmdbMovieDetails devara = new TmdbMovieDetails();
        devara.setTmdbId(842675L);
        devara.setTitle("Devara: Part 1");
        devara.setOriginalTitle("Devara: Part 1");
        devara.setOverview("A fearless warrior embarks on a dangerous journey by sea to safeguard the life and freedom of his people from treacherous pirates.");
        devara.setGenre("Action / Drama / Adventure");
        devara.setLanguage("Telugu, Hindi, Tamil");
        devara.setOriginalLanguage("te");
        devara.setDuration(170);
        devara.setReleaseDate("2024-09-27");
        devara.setRating("UA 16+");
        devara.setCastInfo("Jr NTR, Janhvi Kapoor, Saif Ali Khan, Prakash Raj");
        devara.setDirector("Koratala Siva");
        devara.setPosterUrl("https://image.tmdb.org/t/p/w500/ndRs5lADYm0PeVjuOxaMcInY0o2.jpg");
        devara.setBackdropUrl("https://image.tmdb.org/t/p/original/A216hw7q0O44fH9Z86m1kZzK31k.jpg");
        devara.setTrailerUrl("https://www.youtube.com/watch?v=gS6zD5w0_fA");
        devara.setVoteAverage(8.3);
        devara.setVoteCount(22000);
        devara.setTrailerAvailable(true);
        devara.setTag("NOW_PLAYING");
        OFFLINE_CATALOG.add(devara);

        // They Call Him OG (1099688) - Upcoming
        TmdbMovieDetails og = new TmdbMovieDetails();
        og.setTmdbId(1099688L);
        og.setTitle("They Call Him OG");
        og.setOriginalTitle("OG - Original Gangster");
        og.setOverview("A ruthless Mumbai underworld kingpin disappears for a decade before making a thunderous return to avenge his fallen comrades and reclaim his empire.");
        og.setGenre("Action / Crime / Thriller");
        og.setLanguage("Telugu, Hindi, Tamil");
        og.setOriginalLanguage("te");
        og.setDuration(168);
        og.setReleaseDate("2025-03-27");
        og.setRating("UA 16+");
        og.setCastInfo("Pawan Kalyan, Emraan Hashmi, Priyanka Arul Mohan, Arjun Das");
        og.setDirector("Sujeeth");
        og.setPosterUrl("https://image.tmdb.org/t/p/w500/oWOJ4VMyF92IEcTmrQO0xFDqeyk.jpg");
        og.setBackdropUrl("https://image.tmdb.org/t/p/original/b33nnKl1GSFbao8l3urDDgr4YxF.jpg");
        og.setTrailerUrl("https://www.youtube.com/watch?v=gT8v_vA9b3g");
        og.setVoteAverage(9.1);
        og.setVoteCount(18200);
        og.setTrailerAvailable(true);
        og.setTag("UPCOMING");
        OFFLINE_CATALOG.add(og);

        // Game Changer (933260) - Upcoming
        TmdbMovieDetails gc = new TmdbMovieDetails();
        gc.setTmdbId(933260L);
        gc.setTitle("Game Changer");
        gc.setOriginalTitle("Game Changer");
        gc.setOverview("An honest IAS officer battles corrupt political forces to reform the electoral governance system and restore power to common citizens.");
        gc.setGenre("Action / Drama / Political");
        gc.setLanguage("Telugu, Tamil, Hindi");
        gc.setOriginalLanguage("te");
        gc.setDuration(165);
        gc.setReleaseDate("2025-01-10");
        gc.setRating("UA");
        gc.setCastInfo("Ram Charan, Kiara Advani, SJ Suryah, Anjali");
        gc.setDirector("S. Shankar");
        gc.setPosterUrl("https://upload.wikimedia.org/wikipedia/en/6/6a/Game_Changer_Telugu.jpg");
        gc.setBackdropUrl("https://image.tmdb.org/t/p/original/2KGxQFV9Wp1MshPBf8BuqW0VAu3.jpg");
        gc.setTrailerUrl("https://www.youtube.com/watch?v=Gk7N9v2_X3k");
        gc.setVoteAverage(8.6);
        gc.setVoteCount(19400);
        gc.setTrailerAvailable(true);
        gc.setTag("UPCOMING");
        OFFLINE_CATALOG.add(gc);

        // --- 2. HINDI ---
        // Stree 2 (1112426)
        TmdbMovieDetails stree2 = new TmdbMovieDetails();
        stree2.setTmdbId(1112426L);
        stree2.setTitle("Stree 2");
        stree2.setOriginalTitle("Stree 2: Sarkate Ka Aatank");
        stree2.setOverview("The town of Chanderi is haunted once more, this time by a terrifying headless entity that abducts young women.");
        stree2.setGenre("Horror / Comedy");
        stree2.setLanguage("Hindi");
        stree2.setOriginalLanguage("hi");
        stree2.setDuration(149);
        stree2.setReleaseDate("2024-08-15");
        stree2.setRating("UA 16+");
        stree2.setCastInfo("Rajkummar Rao, Shraddha Kapoor, Pankaj Tripathi, Abhishek Banerjee");
        stree2.setDirector("Amar Kaushik");
        stree2.setPosterUrl("https://image.tmdb.org/t/p/w500/nfnhwfUEFuSOxxf4jDdBlY6Lccw.jpg");
        stree2.setBackdropUrl("https://image.tmdb.org/t/p/original/bylMuw2iUu1D4PzO1Wb2iN1q7Z5.jpg");
        stree2.setTrailerUrl("https://www.youtube.com/watch?v=KVn5iT8p7h8");
        stree2.setVoteAverage(8.6);
        stree2.setVoteCount(28000);
        stree2.setTrailerAvailable(true);
        stree2.setTag("NOW_PLAYING");
        OFFLINE_CATALOG.add(stree2);

        // Singham Again (950387)
        TmdbMovieDetails singham = new TmdbMovieDetails();
        singham.setTmdbId(950387L);
        singham.setTitle("Singham Again");
        singham.setOriginalTitle("Singham Again");
        singham.setOverview("Bajirao Singham journeys across international borders to save his wife while assembling India's greatest cop universe heroes.");
        singham.setGenre("Action / Drama");
        singham.setLanguage("Hindi");
        singham.setOriginalLanguage("hi");
        singham.setDuration(144);
        singham.setReleaseDate("2024-11-01");
        singham.setRating("UA 16+");
        singham.setCastInfo("Ajay Devgn, Kareena Kapoor, Deepika Padukone, Akshay Kumar, Ranveer Singh");
        singham.setDirector("Rohit Shetty");
        singham.setPosterUrl("https://upload.wikimedia.org/wikipedia/en/0/04/Singham_Again_poster.jpg");
        singham.setBackdropUrl("https://image.tmdb.org/t/p/original/bylMuw2iUu1D4PzO1Wb2iN1q7Z5.jpg");
        singham.setTrailerUrl("https://www.youtube.com/watch?v=fM8V5lV3D_0");
        singham.setVoteAverage(8.1);
        singham.setVoteCount(15200);
        singham.setTrailerAvailable(true);
        singham.setTag("POPULAR");
        OFFLINE_CATALOG.add(singham);

        // --- 3. TAMIL ---
        // The Greatest of All Time (GOAT) (1051891)
        TmdbMovieDetails goat = new TmdbMovieDetails();
        goat.setTmdbId(1051891L);
        goat.setTitle("The Greatest of All Time");
        goat.setOriginalTitle("The GOAT");
        goat.setOverview("A retired elite field agent from Special Anti-Terrorist Squad is pulled back into action when an old adversary targets his family.");
        goat.setGenre("Action / Sci-Fi / Thriller");
        goat.setLanguage("Tamil, Telugu, Hindi");
        goat.setOriginalLanguage("ta");
        goat.setDuration(179);
        goat.setReleaseDate("2024-09-05");
        goat.setRating("UA 16+");
        goat.setCastInfo("Vijay, Prashanth, Prabhu Deva, Sneha");
        goat.setDirector("Venkat Prabhu");
        goat.setPosterUrl("https://upload.wikimedia.org/wikipedia/en/1/1e/The_Greatest_of_All_Time.jpg");
        goat.setBackdropUrl("https://image.tmdb.org/t/p/original/A216hw7q0O44fH9Z86m1kZzK31k.jpg");
        goat.setTrailerUrl("https://www.youtube.com/watch?v=jW2m9_tXbT0");
        goat.setVoteAverage(8.4);
        goat.setVoteCount(21000);
        goat.setTrailerAvailable(true);
        goat.setTag("NOW_PLAYING");
        OFFLINE_CATALOG.add(goat);

        // Vettaiyan (1084199)
        TmdbMovieDetails vettaiyan = new TmdbMovieDetails();
        vettaiyan.setTmdbId(1084199L);
        vettaiyan.setTitle("Vettaiyan");
        vettaiyan.setOriginalTitle("Vettaiyan");
        vettaiyan.setOverview("A revered encounter specialist confronts institutional corruption in the educational sector while probing an unexpected extrajudicial shootout.");
        vettaiyan.setGenre("Action / Drama / Crime");
        vettaiyan.setLanguage("Tamil, Telugu, Hindi");
        vettaiyan.setOriginalLanguage("ta");
        vettaiyan.setDuration(163);
        vettaiyan.setReleaseDate("2024-10-10");
        vettaiyan.setRating("UA 16+");
        vettaiyan.setCastInfo("Rajinikanth, Amitabh Bachchan, Fahadh Faasil, Rana Daggubati");
        vettaiyan.setDirector("T. J. Gnanavel");
        vettaiyan.setPosterUrl("https://upload.wikimedia.org/wikipedia/en/6/68/Vettaiyan_poster.jpg");
        vettaiyan.setBackdropUrl("https://image.tmdb.org/t/p/original/b33nnKl1GSFbao8l3urDDgr4YxF.jpg");
        vettaiyan.setTrailerUrl("https://www.youtube.com/watch?v=FqG7W0Y3A7M");
        vettaiyan.setVoteAverage(8.2);
        vettaiyan.setVoteCount(17800);
        vettaiyan.setTrailerAvailable(true);
        vettaiyan.setTag("POPULAR");
        OFFLINE_CATALOG.add(vettaiyan);

        // --- 4. MALAYALAM ---
        // Manjummel Boys (1143019)
        TmdbMovieDetails manjummel = new TmdbMovieDetails();
        manjummel.setTmdbId(1143019L);
        manjummel.setTitle("Manjummel Boys");
        manjummel.setOriginalTitle("Manjummel Boys");
        manjummel.setOverview("A group of friends embark on a daring rescue mission after one of them slips into the perilous Guna Caves in Kodaikanal.");
        manjummel.setGenre("Adventure / Thriller / Drama");
        manjummel.setLanguage("Malayalam, Telugu, Tamil");
        manjummel.setOriginalLanguage("ml");
        manjummel.setDuration(135);
        manjummel.setReleaseDate("2024-02-22");
        manjummel.setRating("U");
        manjummel.setCastInfo("Soubin Shahir, Sreenath Bhasi, Balu Varghese, Ganapathi");
        manjummel.setDirector("Chidambaram");
        manjummel.setPosterUrl("https://upload.wikimedia.org/wikipedia/en/9/99/Manjummel_Boys_poster.jpg");
        manjummel.setBackdropUrl("https://image.tmdb.org/t/p/original/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg");
        manjummel.setTrailerUrl("https://www.youtube.com/watch?v=geS7Kx3yvH4");
        manjummel.setVoteAverage(8.9);
        manjummel.setVoteCount(33000);
        manjummel.setTrailerAvailable(true);
        manjummel.setTag("POPULAR");
        OFFLINE_CATALOG.add(manjummel);

        // Aavesham (1249289)
        TmdbMovieDetails aavesham = new TmdbMovieDetails();
        aavesham.setTmdbId(1249289L);
        aavesham.setTitle("Aavesham");
        aavesham.setOriginalTitle("Aavesham");
        aavesham.setOverview("Three engineering students in Bengaluru seek protection from college seniors by befriending a notorious yet eccentric local gangster.");
        aavesham.setGenre("Action / Comedy");
        aavesham.setLanguage("Malayalam, Telugu");
        aavesham.setOriginalLanguage("ml");
        aavesham.setDuration(158);
        aavesham.setReleaseDate("2024-04-11");
        aavesham.setRating("UA");
        aavesham.setCastInfo("Fahadh Faasil, Hipzster, Mithun Jai Shankar, Roshan Shanavas");
        aavesham.setDirector("Jithu Madhavan");
        aavesham.setPosterUrl("https://upload.wikimedia.org/wikipedia/en/d/d1/Aavesham.jpg");
        aavesham.setBackdropUrl("https://image.tmdb.org/t/p/original/yDHYTjA3R0ne8e49rNZ2348R0k3.jpg");
        aavesham.setTrailerUrl("https://www.youtube.com/watch?v=FqG7W0Y3A7M");
        aavesham.setVoteAverage(8.7);
        aavesham.setVoteCount(26000);
        aavesham.setTrailerAvailable(true);
        aavesham.setTag("POPULAR");
        OFFLINE_CATALOG.add(aavesham);

        // --- 5. KANNADA ---
        // Kantara: Chapter 1 (1083437) - Upcoming
        TmdbMovieDetails kantara = new TmdbMovieDetails();
        kantara.setTmdbId(1083437L);
        kantara.setTitle("Kantara: Chapter 1");
        kantara.setOriginalTitle("Kantara: Chapter 1 - A Legend");
        kantara.setOverview("The ancient origins of the divine Panjurli and Guliga Daiva during the reign of the Kadamba dynasty in Coastal Karnataka.");
        kantara.setGenre("Action / Mythological / Drama");
        kantara.setLanguage("Kannada, Telugu, Hindi, Tamil");
        kantara.setOriginalLanguage("kn");
        kantara.setDuration(160);
        kantara.setReleaseDate("2025-10-02");
        kantara.setRating("UA 16+");
        kantara.setCastInfo("Rishab Shetty, Jayaram, Rukmini Vasanth");
        kantara.setDirector("Rishab Shetty");
        kantara.setPosterUrl("https://upload.wikimedia.org/wikipedia/en/6/69/Kantara-_Chapter_1_poster.jpg");
        kantara.setBackdropUrl("https://image.tmdb.org/t/p/original/xOMo8BRK7PfcJv9JCnx7s520DRq.jpg");
        kantara.setTrailerUrl("https://www.youtube.com/watch?v=jW2m9_tXbT0");
        kantara.setVoteAverage(9.2);
        kantara.setVoteCount(19000);
        kantara.setTrailerAvailable(true);
        kantara.setTag("UPCOMING");
        OFFLINE_CATALOG.add(kantara);

        // KGF: Chapter 2 (585245)
        TmdbMovieDetails kgf2 = new TmdbMovieDetails();
        kgf2.setTmdbId(585245L);
        kgf2.setTitle("K.G.F: Chapter 2");
        kgf2.setOriginalTitle("K.G.F: Chapter 2");
        kgf2.setOverview("In the blood-soaked Kolar Gold Fields, Rocky's name strikes fear into his foes while government forces view him as a threat to law and order.");
        kgf2.setGenre("Action / Crime / Drama");
        kgf2.setLanguage("Kannada, Telugu, Hindi, Tamil");
        kgf2.setOriginalLanguage("kn");
        kgf2.setDuration(168);
        kgf2.setReleaseDate("2022-04-14");
        kgf2.setRating("UA");
        kgf2.setCastInfo("Yash, Sanjay Dutt, Raveena Tandon, Srinidhi Shetty");
        kgf2.setDirector("Prashanth Neel");
        kgf2.setPosterUrl("https://upload.wikimedia.org/wikipedia/en/d/d0/K.G.F_Chapter_2.jpg");
        kgf2.setBackdropUrl("https://image.tmdb.org/t/p/original/2KGxQFV9Wp1MshPBf8BuqW0VAu3.jpg");
        kgf2.setTrailerUrl("https://www.youtube.com/watch?v=JKa05nyUmuQ");
        kgf2.setVoteAverage(8.7);
        kgf2.setVoteCount(52000);
        kgf2.setTrailerAvailable(true);
        kgf2.setTag("POPULAR");
        OFFLINE_CATALOG.add(kgf2);

        // --- 6. ENGLISH ---
        // Dune: Part Two (693134)
        TmdbMovieDetails dune2 = new TmdbMovieDetails();
        dune2.setTmdbId(693134L);
        dune2.setTitle("Dune: Part Two");
        dune2.setOriginalTitle("Dune: Part Two");
        dune2.setOverview("Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.");
        dune2.setGenre("Sci-Fi / Adventure / Drama");
        dune2.setLanguage("English, Hindi");
        dune2.setOriginalLanguage("en");
        dune2.setDuration(166);
        dune2.setReleaseDate("2024-03-01");
        dune2.setRating("UA 16+");
        dune2.setCastInfo("Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem");
        dune2.setDirector("Denis Villeneuve");
        dune2.setPosterUrl("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg");
        dune2.setBackdropUrl("https://image.tmdb.org/t/p/original/xOMo8BRK7PfcJv9JCnx7s520DRq.jpg");
        dune2.setTrailerUrl("https://www.youtube.com/watch?v=Way9Dexny3w");
        dune2.setVoteAverage(8.8);
        dune2.setVoteCount(42000);
        dune2.setTrailerAvailable(true);
        dune2.setTag("NOW_PLAYING");
        OFFLINE_CATALOG.add(dune2);

        // Deadpool & Wolverine (533535)
        TmdbMovieDetails dw = new TmdbMovieDetails();
        dw.setTmdbId(533535L);
        dw.setTitle("Deadpool & Wolverine");
        dw.setOriginalTitle("Deadpool & Wolverine");
        dw.setOverview("A listless Wade Wilson toils away in civilian life with his days as the morally flexible mercenary, Deadpool, behind him. But when an existential threat emerges, he reluctantly recruits Wolverine.");
        dw.setGenre("Action / Comedy / Sci-Fi");
        dw.setLanguage("English, Hindi, Telugu");
        dw.setOriginalLanguage("en");
        dw.setDuration(128);
        dw.setReleaseDate("2024-07-26");
        dw.setRating("A");
        dw.setCastInfo("Ryan Reynolds, Hugh Jackman, Emma Corrin, Matthew Macfadyen");
        dw.setDirector("Shawn Levy");
        dw.setPosterUrl("https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg");
        dw.setBackdropUrl("https://image.tmdb.org/t/p/original/yDHYTjA3R0ne8e49rNZ2348R0k3.jpg");
        dw.setTrailerUrl("https://www.youtube.com/watch?v=73_1biulkYk");
        dw.setVoteAverage(8.4);
        dw.setVoteCount(33500);
        dw.setTrailerAvailable(true);
        dw.setTag("NOW_PLAYING");
        OFFLINE_CATALOG.add(dw);

        // Gladiator II (558449)
        TmdbMovieDetails glad = new TmdbMovieDetails();
        glad.setTmdbId(558449L);
        glad.setTitle("Gladiator II");
        glad.setOriginalTitle("Gladiator II");
        glad.setOverview("Years after witnessing the death of Maximus at the hands of his uncle, Lucius must enter the Colosseum after the emperors of Rome conquer his home.");
        glad.setGenre("Action / Adventure / Historical");
        glad.setLanguage("English, Hindi");
        glad.setOriginalLanguage("en");
        glad.setDuration(148);
        glad.setReleaseDate("2024-11-15");
        glad.setRating("A");
        glad.setCastInfo("Paul Mescal, Pedro Pascal, Denzel Washington, Connie Nielsen");
        glad.setDirector("Ridley Scott");
        glad.setPosterUrl("https://image.tmdb.org/t/p/w500/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg");
        glad.setBackdropUrl("https://image.tmdb.org/t/p/original/euYIwmwkmz95mnExvufCukNGwyG.jpg");
        glad.setTrailerUrl("https://www.youtube.com/watch?v=4rgYUipGJNo");
        glad.setVoteAverage(8.2);
        glad.setVoteCount(18500);
        glad.setTrailerAvailable(true);
        glad.setTag("NOW_PLAYING");
        OFFLINE_CATALOG.add(glad);
    }

    private List<TmdbSearchResult> searchOfflineCatalog(String query) {
        String q = query.toLowerCase().trim();
        return OFFLINE_CATALOG.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(q) ||
                             m.getOriginalTitle().toLowerCase().contains(q) ||
                             m.getGenre().toLowerCase().contains(q))
                .map(this::mapDetailsToSearchResult)
                .collect(Collectors.toList());
    }

    private TmdbMovieDetails getOfflineMovieDetails(Long tmdbId) {
        return OFFLINE_CATALOG.stream()
                .filter(m -> m.getTmdbId().equals(tmdbId))
                .findFirst()
                .orElseGet(() -> {
                    TmdbMovieDetails generic = new TmdbMovieDetails();
                    generic.setTmdbId(tmdbId);
                    generic.setTitle("Movie #" + tmdbId);
                    generic.setOverview("Exciting cinematic experience available now in CineWave theaters.");
                    generic.setGenre("Action / Drama");
                    generic.setLanguage("English, Hindi");
                    generic.setOriginalLanguage("en");
                    generic.setDuration(145);
                    generic.setRating("UA");
                    generic.setPosterUrl("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg");
                    generic.setBackdropUrl("https://image.tmdb.org/t/p/original/xOMo8BRK7PfcJv9JCnx7s520DRq.jpg");
                    generic.setTrailerAvailable(false);
                    generic.setTag("NOW_PLAYING");
                    return generic;
                });
    }
}
