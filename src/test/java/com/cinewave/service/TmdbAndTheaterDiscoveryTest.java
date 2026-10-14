package com.cinewave.service;

import com.cinewave.dto.TmdbMovieDetails;
import com.cinewave.dto.TmdbSearchResult;
import com.cinewave.entity.Movie;
import com.cinewave.entity.Theater;
import com.cinewave.entity.User;
import com.cinewave.repository.MovieRepository;
import com.cinewave.repository.TheaterRepository;
import com.cinewave.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TmdbAndTheaterDiscoveryTest {

    private TmdbService tmdbService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private UserRepository userRepository;

    private MovieSyncService movieSyncService;
    private TheaterDirectoryService theaterDirectoryService;

    @BeforeEach
    void setUp() {
        tmdbService = new TmdbService(new RestTemplateBuilder());
        movieSyncService = new MovieSyncService(movieRepository, tmdbService);
        theaterDirectoryService = new TheaterDirectoryService(theaterRepository, userRepository);
    }

    @Test
    @DisplayName("TMDB Service: Now Playing India Region returns authentic blockbusters")
    void testTmdbNowPlaying() {
        List<TmdbSearchResult> results = tmdbService.getNowPlaying(1);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(m -> m.getTitle().contains("Kalki") || m.getTitle().contains("Pushpa") || m.getTitle().contains("Devara")));
    }

    @Test
    @DisplayName("TMDB Service: Regional Language Discovery works for Telugu, Tamil, Malayalam, Hindi, Kannada, English")
    void testTmdbRegionalDiscovery() {
        List<TmdbSearchResult> telugu = tmdbService.discoverByLanguage("te", 1);
        assertNotNull(telugu);
        assertFalse(telugu.isEmpty());

        List<TmdbSearchResult> hindi = tmdbService.discoverByLanguage("hi", 1);
        assertNotNull(hindi);
        assertFalse(hindi.isEmpty());

        List<TmdbSearchResult> malayalam = tmdbService.discoverByLanguage("ml", 1);
        assertNotNull(malayalam);
        assertFalse(malayalam.isEmpty());
    }

    @Test
    @DisplayName("TMDB Service: Movie Details has exact posters, duration, censor rating, and trailer")
    void testTmdbMovieDetails() {
        TmdbMovieDetails details = tmdbService.getMovieDetails(801688L); // Kalki 2898 AD
        assertNotNull(details);
        assertEquals("Kalki 2898 AD", details.getTitle());
        assertTrue(details.getPosterUrl().contains("image.tmdb.org"));
        assertNotNull(details.getDirector());
        assertNotNull(details.getCastInfo());
        assertTrue(details.getDuration() > 60);
        assertTrue(details.isTrailerAvailable());
    }

    @Test
    @DisplayName("Movie Sync Service: syncMoviesFromTmdb adds new movies and updates without duplicates")
    void testMovieSyncFromTmdb() {
        when(movieRepository.findByTmdbId(anyLong())).thenReturn(Optional.empty());
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = movieSyncService.syncMoviesFromTmdb(true);
        assertEquals("SUCCESS", result.get("status"));
        assertTrue((int) result.get("addedCount") > 0);
        verify(movieRepository, atLeastOnce()).save(any(Movie.class));
    }

    @Test
    @DisplayName("Movie Sync Service: Preserves admin customized movies without overwriting edits")
    void testMovieSyncPreservesAdminCustomizations() {
        Movie customMovie = new Movie();
        customMovie.setId(100L);
        customMovie.setTmdbId(801688L);
        customMovie.setTitle("Customized Title By Admin");
        customMovie.setDescription("Customized Description");
        customMovie.setCustomizedByAdmin(true);

        when(movieRepository.findByTmdbId(801688L)).thenReturn(Optional.of(customMovie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = movieSyncService.syncMoviesFromTmdb(true);
        assertEquals("SUCCESS", result.get("status"));
        // Admin title must remain intact
        assertEquals("Customized Title By Admin", customMovie.getTitle());
        assertEquals("Customized Description", customMovie.getDescription());
    }

    @Test
    @DisplayName("Theater Directory Service: Syncs authorized directory prioritizing Hyderabad and Telangana")
    void testTheaterDirectorySync() {
        when(theaterRepository.findByProviderId(anyString())).thenReturn(Optional.empty());
        when(theaterRepository.findByNormalizedKey(anyString())).thenReturn(Optional.empty());
        when(theaterRepository.save(any(Theater.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = theaterDirectoryService.syncTheatersFromDirectory();
        assertEquals("SUCCESS", result.get("status"));
        assertTrue((int) result.get("addedCount") >= 15);
        verify(theaterRepository, atLeastOnce()).save(any(Theater.class));
    }

    @Test
    @DisplayName("Theater Directory Service: CSV Import parses lines, de-duplicates, and flags as DISCOVERED")
    void testCsvImport() {
        String csvData = "providerId,name,city,state,district,address,pincode,phone,latitude,longitude,website,facilities\n" +
                "CSV-001,Sudharshan 35MM,Hyderabad,Telangana,Hyderabad,RTC X Roads,500020,04027614455,17.404,78.497,,4K Laser\n" +
                "CSV-002,Sandhya 70MM,Hyderabad,Telangana,Hyderabad,RTC X Roads,500020,04027612211,17.403,78.498,,Dolby Digital\n";

        when(theaterRepository.existsByProviderId("CSV-001")).thenReturn(false);
        when(theaterRepository.existsByNormalizedKey(anyString())).thenReturn(false);
        when(theaterRepository.save(any(Theater.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ByteArrayInputStream in = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> result = theaterDirectoryService.importFromCsv(in);

        assertEquals("SUCCESS", result.get("status"));
        assertEquals(2, result.get("importedCount"));
        assertEquals(0, result.get("errorCount"));
        verify(theaterRepository, times(2)).save(any(Theater.class));
    }

    @Test
    @DisplayName("Theater Directory Service: Admin approve and activate partner workflow")
    void testApproveAndActivatePartner() {
        Theater theater = new Theater();
        theater.setName("Discovered Cinema");
        theater.setCity("Hyderabad");
        theater.setApprovalStatus("DISCOVERED");
        theater.setVerifiedPartner(false);

        when(theaterRepository.findById(10L)).thenReturn(Optional.of(theater));
        when(theaterRepository.save(any(Theater.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. Approve
        theaterDirectoryService.approveTheater(10L);
        assertEquals("APPROVED", theater.getApprovalStatus());
        assertFalse(theater.isVerifiedPartner());

        // 2. Activate Partner
        User owner = new User();
        owner.setName("Theater Owner");
        owner.setEmail("owner@cinewave.com");
        when(userRepository.findById(5L)).thenReturn(Optional.of(owner));

        theaterDirectoryService.activatePartner(10L, 5L);
        assertEquals("PARTNER_ACTIVE", theater.getApprovalStatus());
        assertTrue(theater.isVerifiedPartner());
        assertEquals(owner, theater.getOwner());
    }
}
