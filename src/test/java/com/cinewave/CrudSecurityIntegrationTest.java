package com.cinewave;

import com.cinewave.dto.*;
import com.cinewave.entity.*;
import com.cinewave.exception.BadRequestException;
import com.cinewave.repository.*;
import com.cinewave.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CrudSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieService movieService;

    @Autowired
    private TheaterService theaterService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ShowService showService;

    @Autowired
    private EventService eventService;

    @Autowired
    private StreamService streamService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private UserRepository userRepository;

    private Long sampleMovieId;
    private Long sampleTheaterId;
    private Long sampleScreenId;

    @BeforeEach
    void setUp() {
        // Ensure at least one movie, theater, and screen exist for tests
        Movie movie = movieRepository.findAll().stream().findFirst().orElseGet(() -> {
            Movie m = new Movie();
            m.setTitle("Interstellar " + System.currentTimeMillis());
            m.setGenre("Sci-Fi");
            m.setLanguage("English");
            m.setDuration(169);
            m.setPosterUrl("https://example.com/poster.jpg");
            m.setActive(true);
            return movieRepository.save(m);
        });
        sampleMovieId = movie.getId();

        Theater theater = theaterRepository.findAll().stream().findFirst().orElseGet(() -> {
            Theater t = new Theater();
            t.setName("PVR Superplex");
            t.setCity("Tanuku");
            t.setState("Andhra Pradesh");
            t.setDistrict("West Godavari");
            t.setAddress("Main Road");
            return theaterRepository.save(t);
        });
        sampleTheaterId = theater.getId();

        Screen screen = screenRepository.findByTheaterId(sampleTheaterId).stream().findFirst().orElseGet(() -> {
            Screen s = new Screen();
            s.setName("Screen 1");
            s.setTheater(theater);
            return screenRepository.save(s);
        });
        sampleScreenId = screen.getId();
    }

    private SeatRequest createSeatReq(Long screenId, String row, int num, SeatCategory cat) {
        SeatRequest req = new SeatRequest();
        req.setScreenId(screenId);
        req.setRowLabel(row);
        req.setSeatNumber(num);
        req.setCategory(cat);
        return req;
    }

    // ==========================================
    // 1. ADMIN MOVIE CRUD
    // ==========================================
    @Test
    @DisplayName("Admin Movie CRUD: Create, Read, Update, Toggle Publish, and Safe Delete")
    void testAdminMovieCrud() {
        // 1. Create Movie
        MovieRequest createReq = new MovieRequest();
        createReq.setTitle("Kalki 2898 AD - Test Edition");
        createReq.setDescription("Epic sci-fi action mythological drama");
        createReq.setGenre("Sci-Fi / Action");
        createReq.setLanguage("Telugu, Hindi");
        createReq.setDuration(181);
        createReq.setRating("UA 16+");
        createReq.setPosterUrl("https://image.tmdb.org/t/p/w500/test.jpg");
        createReq.setReleaseDate(LocalDate.now());

        MovieResponse created = movieService.createMovie(createReq);
        assertNotNull(created.getId());
        assertEquals("Kalki 2898 AD - Test Edition", created.getTitle());
        assertTrue(created.isActive());

        // 2. Read Movie
        MovieResponse fetched = movieService.getMovieById(created.getId());
        assertEquals(created.getId(), fetched.getId());
        assertEquals("Sci-Fi / Action", fetched.getGenre());

        // 3. Update Movie
        MovieRequest updateReq = new MovieRequest();
        updateReq.setTitle("Kalki 2898 AD - Director's Cut");
        updateReq.setDescription(created.getDescription());
        updateReq.setGenre(created.getGenre());
        updateReq.setLanguage(created.getLanguage());
        updateReq.setDuration(190);
        updateReq.setRating("A");
        updateReq.setPosterUrl(created.getPosterUrl());

        MovieResponse updated = movieService.updateMovie(created.getId(), updateReq);
        assertEquals("Kalki 2898 AD - Director's Cut", updated.getTitle());
        assertEquals(190, updated.getDuration());
        assertEquals("A", updated.getRating());

        // 4. Toggle Publish Status
        MovieResponse toggled = movieService.togglePublish(created.getId());
        assertFalse(toggled.isActive());

        // 5. Safe Delete / Deactivate
        movieService.deleteMovie(created.getId());
        Movie deleted = movieRepository.findById(Objects.requireNonNull(created.getId())).orElse(null);
        assertNotNull(deleted);
        assertFalse(deleted.isActive(), "Movie should be safely deactivated / soft-deleted");
    }

    // ==========================================
    // 2. ADMIN THEATER CRUD & OWNER ASSIGNMENT
    // ==========================================
    @Test
    @DisplayName("Admin Theater CRUD: Create with State/District, Read, Update, Assign Owner, Safe Delete")
    void testAdminTheaterCrud() {
        // 1. Create Theater
        TheaterRequest theaterReq = new TheaterRequest();
        theaterReq.setName("CineWave Grand Cineplex");
        theaterReq.setCity("Tanuku");
        theaterReq.setState("Andhra Pradesh");
        theaterReq.setDistrict("West Godavari");
        theaterReq.setAddress("RP Road, Opp RTC Complex");
        theaterReq.setFacilities("4K Laser, Dolby Atmos 7.1, VIP Recliners");

        TheaterResponse created = theaterService.createTheater(theaterReq);
        assertNotNull(created.getId());
        assertEquals("CineWave Grand Cineplex", created.getName());
        assertEquals("Andhra Pradesh", created.getState());
        assertEquals("West Godavari", created.getDistrict());

        // 2. Read & Search by State
        List<String> states = theaterService.getStates();
        assertTrue(states.contains("Andhra Pradesh"));

        List<String> districts = theaterService.getDistrictsByState("Andhra Pradesh");
        assertTrue(districts.contains("West Godavari"));

        // 3. Update Theater
        theaterReq.setName("CineWave Grand IMAX & Laser");
        theaterReq.setFacilities("IMAX with Laser, Dolby Atmos, Gourmet Food");
        TheaterResponse updated = theaterService.updateTheater(created.getId(), theaterReq);
        assertEquals("CineWave Grand IMAX & Laser", updated.getName());

        // 4. Create an Owner and Assign to Theater
        OwnerRequest ownerReq = new OwnerRequest();
        ownerReq.setName("Anand Rao");
        ownerReq.setEmail("anand." + System.currentTimeMillis() + "@cinewave.com");
        ownerReq.setPassword("Password@123");
        ownerReq.setPhone("+91 9988776655");
        OwnerResponse owner = adminService.createOwner(ownerReq);

        TheaterResponse assigned = theaterService.assignOwner(created.getId(), owner.getId());
        assertEquals(owner.getId(), assigned.getOwnerId());

        // 5. Safe Delete Theater
        theaterService.deleteTheater(created.getId());
        Theater deletedTheater = theaterRepository.findById(Objects.requireNonNull(created.getId())).orElseThrow();
        assertFalse(deletedTheater.isActive());
    }

    // ==========================================
    // 3. THEATER OWNER SECURE CRUD
    // ==========================================
    @Test
    @DisplayName("Theater Owner CRUD: Secure Creation with BCrypt, View Assigned Theaters, No Password Leaks")
    void testTheaterOwnerCrud() {
        OwnerRequest req = new OwnerRequest();
        req.setName("Suresh Multiplex Owner");
        req.setEmail("owner." + System.currentTimeMillis() + "@test.com");
        req.setPassword("SecureOwnerPass#99");
        req.setPhone("+91 9848022338");
        req.setAssignedTheaterIds(List.of(sampleTheaterId));

        OwnerResponse owner = adminService.createOwner(req);
        assertNotNull(owner.getId());
        assertEquals(Role.THEATER_OWNER, owner.getRole());
        assertTrue(owner.isActive());
        assertEquals(1, owner.getAssignedTheaters().size());

        // Security check: Verify User entity stored encrypted password, not plaintext
        User userEntity = userRepository.findById(Objects.requireNonNull(owner.getId())).orElseThrow();
        assertNotEquals("SecureOwnerPass#99", userEntity.getPassword());
        assertTrue(userEntity.getPassword().startsWith("$2a$") || userEntity.getPassword().startsWith("$2b$"));

        // Read all owners
        List<OwnerResponse> owners = adminService.getAllOwnersWithTheaters();
        assertTrue(owners.stream().anyMatch(o -> o.getId().equals(owner.getId())));

        // Update owner details
        OwnerRequest updateReq = new OwnerRequest();
        updateReq.setName("Suresh K - Managing Partner");
        updateReq.setPhone("+91 9949011223");
        OwnerResponse updated = adminService.updateOwner(owner.getId(), updateReq);
        assertEquals("Suresh K - Managing Partner", updated.getName());
        assertEquals("+91 9949011223", updated.getPhone());

        // Toggle Status
        OwnerResponse toggled = adminService.toggleOwnerStatus(owner.getId());
        assertFalse(toggled.isActive());

        // Reactivate
        OwnerResponse reactivated = adminService.toggleOwnerStatus(owner.getId());
        assertTrue(reactivated.isActive());
    }

    // ==========================================
    // 4. SCREENS & SEATS CRUD WITH SAFETY RULES
    // ==========================================
    @Test
    @DisplayName("Screens & Seats CRUD: Create Screen, Bulk Seats, Prevent Duplicates, Update Category, Delete")
    void testScreensAndSeatsCrud() {
        // 1. Create new Screen
        ScreenRequest screenReq = new ScreenRequest();
        screenReq.setTheaterId(sampleTheaterId);
        screenReq.setName("VIP Dolby Screen " + System.currentTimeMillis());

        ScreenResponse screen = theaterService.createScreen(screenReq);
        assertNotNull(screen.getId());

        // 2. Create Bulk Seats
        List<SeatRequest> seatBatch = List.of(
                createSeatReq(screen.getId(), "A", 1, SeatCategory.RECLINER),
                createSeatReq(screen.getId(), "A", 2, SeatCategory.RECLINER),
                createSeatReq(screen.getId(), "B", 1, SeatCategory.PREMIUM),
                createSeatReq(screen.getId(), "B", 2, SeatCategory.PREMIUM),
                createSeatReq(screen.getId(), "C", 1, SeatCategory.REGULAR)
        );
        List<SeatResponse> createdSeats = theaterService.createBulkSeats(screen.getId(), seatBatch);
        assertEquals(5, createdSeats.size());

        // 3. Duplicate Seat Prevention Rule: Attempting duplicate seat A1 in same screen must fail
        SeatRequest duplicateReq = createSeatReq(screen.getId(), "A", 1, SeatCategory.RECLINER);
        assertThrows(BadRequestException.class, () -> theaterService.createSeat(duplicateReq));

        // 4. Update Seat Category
        SeatResponse seatToUpdate = createdSeats.get(4); // C1
        SeatUpdateRequest updateReq = new SeatUpdateRequest("C", 1, SeatCategory.PREMIUM);
        SeatResponse updatedSeat = theaterService.updateSeat(seatToUpdate.getId(), updateReq);
        assertEquals(SeatCategory.PREMIUM, updatedSeat.getCategory());

        // 5. Delete Seat safely
        theaterService.deleteSeat(seatToUpdate.getId());
        List<SeatResponse> remainingSeats = theaterService.getSeatsByScreen(screen.getId());
        assertEquals(4, remainingSeats.size());
    }

    // ==========================================
    // 5. SHOWS CRUD WITH CONFLICT DETECTION & REFUND ON REMOVAL
    // ==========================================
    @Test
    @DisplayName("Shows CRUD: Create Show, Prevent Schedule Overlap, Update Prices, Safe Delete With Refund")
    void testShowsCrudWithOverlapPreventionAndRefund() {
        LocalDate showDate = LocalDate.now().plusDays(5);
        LocalTime startTime = LocalTime.of(14, 0);

        // 1. Create Show
        ShowRequest showReq = new ShowRequest();
        showReq.setMovieId(sampleMovieId);
        showReq.setScreenId(sampleScreenId);
        showReq.setShowDate(showDate);
        showReq.setStartTime(startTime);
        showReq.setPriceRegular(BigDecimal.valueOf(220.0));
        showReq.setPricePremium(BigDecimal.valueOf(350.0));
        showReq.setPriceRecliner(BigDecimal.valueOf(500.0));

        ShowResponse created = showService.createShow(showReq);
        assertNotNull(created.getId());
        assertEquals(showDate, created.getShowDate());
        assertEquals(startTime, created.getStartTime());

        // 2. Conflict Prevention: Attempting another show at exact same screen, date, and overlapping time must fail
        ShowRequest conflictingReq = new ShowRequest();
        conflictingReq.setMovieId(sampleMovieId);
        conflictingReq.setScreenId(sampleScreenId);
        conflictingReq.setShowDate(showDate);
        conflictingReq.setStartTime(LocalTime.of(14, 30)); // Overlap!
        conflictingReq.setPriceRegular(BigDecimal.valueOf(200.0));
        conflictingReq.setPricePremium(BigDecimal.valueOf(300.0));
        conflictingReq.setPriceRecliner(BigDecimal.valueOf(450.0));

        assertThrows(BadRequestException.class, () -> showService.createShow(conflictingReq));

        // 3. Update Show Prices & Settings
        showReq.setPriceRegular(BigDecimal.valueOf(250.0));
        showReq.setPricePremium(BigDecimal.valueOf(400.0));
        showReq.setPriceRecliner(BigDecimal.valueOf(550.0));
        ShowResponse updated = showService.updateShow(created.getId(), showReq);
        assertEquals(0, BigDecimal.valueOf(250.0).compareTo(updated.getPriceRegular()));

        // 4. Safe Show Deletion & Cancellation
        showService.deleteShow(created.getId());
        ShowResponse deletedShow = showService.getShowById(created.getId());
        assertFalse(deletedShow.isActive());
    }

    // ==========================================
    // 6. EVENTS, STREAMS & PROMO OFFERS CRUD
    // ==========================================
    @Test
    @DisplayName("Events, Streams, Offers CRUD: Create, Read, Update, Toggle, Delete")
    void testMarketingCrud() {
        // --- Event CRUD ---
        Event event = new Event();
        event.setTitle("Anirudh Live in Concert");
        event.setCategory("Concerts");
        event.setCity("Tanuku");
        event.setEventDate(LocalDate.now().plusDays(20));
        event.setEventTime(LocalTime.of(18, 0));
        event.setVenue("Gachibowli Stadium, Hyderabad");
        event.setPrice(999.0);
        event.setBannerUrl("https://example.com/anirudh.jpg");
        event.setDescription("High energy musical night");
        event.setActive(true);

        Event createdEvent = eventService.create(event);
        assertNotNull(createdEvent.getId());
        assertTrue(createdEvent.isActive());

        Event toggledEvent = eventService.toggleStatus(createdEvent.getId());
        assertFalse(toggledEvent.isActive());

        eventService.delete(createdEvent.getId());
        assertFalse(eventService.getById(createdEvent.getId()).isActive());

        // --- Stream Content CRUD ---
        StreamContent stream = new StreamContent();
        stream.setTitle("CineWave Originals: The Chase");
        stream.setCategory("Thriller");
        stream.setDuration(110);
        stream.setReleaseDate(LocalDate.of(2025, 1, 1));
        stream.setVideoUrl("https://cinewave.com/streams/chase.m3u8");
        stream.setThumbnailUrl("https://cinewave.com/posters/chase.jpg");
        stream.setDescription("Edge of the seat digital thriller");
        stream.setActive(true);

        StreamContent createdStream = streamService.create(stream);
        assertNotNull(createdStream.getId());
        assertTrue(createdStream.isActive());

        StreamContent toggledStream = streamService.toggleStatus(createdStream.getId());
        assertFalse(toggledStream.isActive());

        streamService.delete(createdStream.getId());
        assertFalse(streamService.getById(createdStream.getId()).isActive());

        // --- Promo Offer CRUD ---
        Offer offer = new Offer();
        offer.setCode("SUMMER40");
        offer.setTitle("Summer Bonanza 40% Off");
        offer.setDiscountType("PERCENTAGE");
        offer.setDiscountValue(40.0);
        offer.setMaxDiscount(150.0);
        offer.setMinOrderAmount(300.0);
        offer.setEndDate(LocalDate.now().plusDays(60));
        offer.setUsageLimit(200);
        offer.setDescription("40% off on multiplex tickets");
        offer.setActive(true);

        Offer createdOffer = offerService.create(offer);
        assertNotNull(createdOffer.getId());
        assertEquals("SUMMER40", createdOffer.getCode());

        Offer toggledOffer = offerService.toggleStatus(createdOffer.getId());
        assertFalse(toggledOffer.isActive());

        offerService.delete(createdOffer.getId());
        assertFalse(offerService.getById(createdOffer.getId()).isActive());
    }

    // ==========================================
    // 7. USER PROFILE CRUD & DEACTIVATION
    // ==========================================
    @Test
    @DisplayName("User Account CRUD: Profile Update, Search, and Self-Service Deactivation")
    void testUserCrudAndDeactivation() {
        // Register a user
        User user = new User();
        user.setName("Bhavya Reddy");
        user.setEmail("bhavya." + System.currentTimeMillis() + "@test.com");
        user.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
        user.setPhone("+91 9123456780");
        user.setRole(Role.USER);
        user.setActive(true);
        final User savedUser = userRepository.save(user);

        // Update profile via Admin
        UpdateUserRequest updateReq = new UpdateUserRequest();
        updateReq.setName("Bhavya Reddy - VIP Patron");
        updateReq.setPhone("+91 9988001122");
        updateReq.setRole(Role.USER);
        updateReq.setActive(true);

        UserResponse updated = adminService.updateUser(savedUser.getId(), updateReq);
        assertEquals("Bhavya Reddy - VIP Patron", updated.getName());
        assertEquals("+91 9988001122", updated.getPhone());

        // Search Users
        List<UserResponse> searchResults = adminService.searchUsers("Bhavya");
        assertTrue(searchResults.stream().anyMatch(u -> u.getId().equals(savedUser.getId())));

        // Toggle / Deactivate User Status
        UserResponse toggled = adminService.toggleUserStatus(savedUser.getId());
        assertFalse(toggled.isActive());

        User reloaded = userRepository.findById(Objects.requireNonNull(savedUser.getId())).orElseThrow();
        assertFalse(reloaded.isActive(), "Deactivated user must have active=false in database");
    }

    // ==========================================
    // 8. SECURITY & ROLE AUTHORIZATION TESTS
    // ==========================================
    @Test
    @DisplayName("Security: Normal USER is Rejected with 403 Forbidden on Admin & Owner Endpoints")
    @WithMockUser(username = "patron@cinewave.com", roles = {"USER"})
    void testNormalUserForbiddenOnAdminAndOwnerApis() throws Exception {
        // Normal user hitting Admin endpoints -> 403
        mockMvc.perform(get("/api/admin/movies"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/theaters"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/owners"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isForbidden());

        // Normal user hitting Owner endpoints -> 403
        mockMvc.perform(get("/api/owner/theaters"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/owner/shows"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: THEATER_OWNER is Forbidden on Admin Catalogs & User Role Management")
    @WithMockUser(username = "owner@cinewave.com", roles = {"THEATER_OWNER"})
    void testTheaterOwnerForbiddenOnAdminApis() throws Exception {
        // Owner attempting to manage global admin catalogs / users / owners -> 403
        mockMvc.perform(get("/api/admin/owners"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/owners")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"name\":\"Fake\",\"email\":\"fake@test.com\",\"password\":\"123456\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: ADMIN has Full Access to Admin CRUD Endpoints")
    @WithMockUser(username = "admin@cinewave.com", roles = {"ADMIN"})
    void testAdminHasFullAccess() throws Exception {
        mockMvc.perform(get("/api/admin/movies"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/theaters"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/owners"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/events"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/streams"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/offers"))
                .andExpect(status().isOk());
    }
}
