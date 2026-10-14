package com.cinewave.config;

import com.cinewave.entity.*;
import com.cinewave.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds initial demo data (movies, theaters, screens, seats, and shows)
 * if the database is empty, allowing the user to experience the full platform immediately.
 */
@Component
@Order(2) // Runs after AdminInitializer (Order 1)
@SuppressWarnings("null")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final StreamContentRepository streamContentRepository;
    private final EventRepository eventRepository;
    private final OfferRepository offerRepository;

    public DataSeeder(
            MovieRepository movieRepository,
            TheaterRepository theaterRepository,
            ScreenRepository screenRepository,
            SeatRepository seatRepository,
            ShowRepository showRepository,
            StreamContentRepository streamContentRepository,
            EventRepository eventRepository,
            OfferRepository offerRepository) {
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.streamContentRepository = streamContentRepository;
        this.eventRepository = eventRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (movieRepository.count() == 0) {
            log.info("Populating CineWave with initial catalog: movies, theaters, screens, seats, and shows...");
            seedMoviesTheatersShows();
        } else {
            log.info("Database already contains movie data, skipping movie/theater DataSeeder.");
        }

        if (streamContentRepository.count() == 0) {
            seedStreams();
        }

        if (eventRepository.count() == 0) {
            seedEvents();
        }

        if (offerRepository.count() == 0) {
            seedOffers();
        }

        ensureUpcomingShowsForAllMovies();
    }

    private void seedMoviesTheatersShows() {

        // 1. Create Movies with Verified TMDB Posters
        Movie dune = new Movie();
        dune.setTitle("Dune: Part Two");
        dune.setDescription("Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the universe, he endeavors to prevent a terrible future only he can foresee.");
        dune.setGenre("Sci-Fi / Adventure / Drama");
        dune.setDuration(166);
        dune.setLanguage("English, Hindi");
        dune.setReleaseDate(LocalDate.now().minusDays(15));
        dune.setRating("UA 16+");
        dune.setCastInfo("Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem");
        dune.setPosterUrl("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg");
        dune.setBackdropUrl("https://image.tmdb.org/t/p/original/xOMo8BRK7PfcJv9JCnx7s520DRq.jpg");
        dune.setTrailerUrl("https://www.youtube.com/watch?v=Way9Dexny3w");
        dune.setTmdbId(693134L);
        movieRepository.save(dune);

        Movie oppenheimer = new Movie();
        oppenheimer.setTitle("Oppenheimer");
        oppenheimer.setDescription("The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II, exploring both scientific triumph and profound moral dilemmas.");
        oppenheimer.setGenre("Biography / Drama / History");
        oppenheimer.setDuration(180);
        oppenheimer.setLanguage("English");
        oppenheimer.setReleaseDate(LocalDate.now().minusDays(30));
        oppenheimer.setRating("R");
        oppenheimer.setCastInfo("Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.");
        oppenheimer.setPosterUrl("https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg");
        oppenheimer.setBackdropUrl("https://image.tmdb.org/t/p/original/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg");
        oppenheimer.setTrailerUrl("https://www.youtube.com/watch?v=uYPbbksJxIg");
        oppenheimer.setTmdbId(872585L);
        movieRepository.save(oppenheimer);

        Movie spiderman = new Movie();
        spiderman.setTitle("Spider-Man: Across the Spider-Verse");
        spiderman.setDescription("Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence. When heroes clash on handling a new threat, Miles must redefine what it means to be a hero.");
        spiderman.setGenre("Animation / Action / Adventure");
        spiderman.setDuration(140);
        spiderman.setLanguage("English, Hindi, Tamil");
        spiderman.setReleaseDate(LocalDate.now().minusDays(10));
        spiderman.setRating("PG");
        spiderman.setCastInfo("Shameik Moore, Hailee Steinfeld, Oscar Isaac");
        spiderman.setPosterUrl("https://image.tmdb.org/t/p/w500/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg");
        spiderman.setBackdropUrl("https://image.tmdb.org/t/p/original/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg");
        spiderman.setTrailerUrl("https://www.youtube.com/watch?v=cqGjhVJWtEg");
        spiderman.setTmdbId(569094L);
        movieRepository.save(spiderman);

        Movie interstellar = new Movie();
        interstellar.setTitle("Interstellar");
        interstellar.setDescription("When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft along with a team of researchers to find a new planet for humans.");
        interstellar.setGenre("Sci-Fi / Adventure / Drama");
        interstellar.setDuration(169);
        interstellar.setLanguage("English, Hindi");
        interstellar.setReleaseDate(LocalDate.now().minusDays(5));
        interstellar.setRating("PG-13");
        interstellar.setCastInfo("Matthew McConaughey, Anne Hathaway, Jessica Chastain");
        interstellar.setPosterUrl("https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg");
        interstellar.setBackdropUrl("https://image.tmdb.org/t/p/original/rAiYTsqJJR9asvkDq9Aq8GBy0xy.jpg");
        interstellar.setTrailerUrl("https://www.youtube.com/watch?v=zSWdZVtXT7E");
        interstellar.setTmdbId(157336L);
        movieRepository.save(interstellar);

        Movie darkKnight = new Movie();
        darkKnight.setTitle("The Dark Knight");
        darkKnight.setDescription("When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.");
        darkKnight.setGenre("Action / Crime / Drama");
        darkKnight.setDuration(152);
        darkKnight.setLanguage("English, Hindi");
        darkKnight.setReleaseDate(LocalDate.now().minusDays(20));
        darkKnight.setRating("UA 16+");
        darkKnight.setCastInfo("Christian Bale, Heath Ledger, Aaron Eckhart, Michael Caine");
        darkKnight.setPosterUrl("https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg");
        darkKnight.setBackdropUrl("https://image.tmdb.org/t/p/original/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg");
        darkKnight.setTrailerUrl("https://www.youtube.com/watch?v=EXeTwQWrcwY");
        darkKnight.setTmdbId(155L);
        movieRepository.save(darkKnight);

        Movie deadpool = new Movie();
        deadpool.setTitle("Deadpool & Wolverine");
        deadpool.setDescription("Wolverine is recovering from his injuries when he crosses paths with the loudmouth Deadpool. They team up to defeat a common enemy in a wild multiverse ride filled with humor and mayhem.");
        deadpool.setGenre("Action / Comedy / Sci-Fi");
        deadpool.setDuration(128);
        deadpool.setLanguage("English, Hindi, Telugu");
        deadpool.setReleaseDate(LocalDate.now().minusDays(2));
        deadpool.setRating("A");
        deadpool.setCastInfo("Ryan Reynolds, Hugh Jackman, Emma Corrin, Matthew Macfadyen");
        deadpool.setPosterUrl("https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg");
        deadpool.setBackdropUrl("https://image.tmdb.org/t/p/original/yDHYTjA3R0ne8e49rNZ2348R0k3.jpg");
        deadpool.setTrailerUrl("https://www.youtube.com/watch?v=73_1biulkYk");
        deadpool.setTmdbId(533535L);
        movieRepository.save(deadpool);

        // 2. Create Theaters
        Theater pvrDelhi = new Theater();
        pvrDelhi.setName("PVR ICON: Ambience Mall");
        pvrDelhi.setCity("Gurugram");
        pvrDelhi.setAddress("4th Floor, Ambience Mall, NH-8");
        theaterRepository.save(pvrDelhi);

        Theater inoxMumbai = new Theater();
        inoxMumbai.setName("INOX Megaplex: Phoenix Palladium");
        inoxMumbai.setCity("Mumbai");
        inoxMumbai.setAddress("Senapati Bapat Marg, Lower Parel");
        theaterRepository.save(inoxMumbai);

        Theater cinepolisBlr = new Theater();
        cinepolisBlr.setName("Cinepolis VIP: Forum Mall");
        cinepolisBlr.setCity("Bengaluru");
        cinepolisBlr.setAddress("Koramangala, Hosur Main Road");
        theaterRepository.save(cinepolisBlr);

        // 3. Create Screens & Seats for each Theater
        List<Theater> theaters = List.of(pvrDelhi, inoxMumbai, cinepolisBlr);
        List<Screen> allScreens = new ArrayList<>();

        for (Theater t : theaters) {
            Screen s1 = createScreenWithSeats(t, "Screen 1 - IMAX Laser");
            Screen s2 = createScreenWithSeats(t, "Screen 2 - Dolby Atmos");
            Screen s3 = createScreenWithSeats(t, "Screen 3 - 4DX Experience");
            allScreens.add(s1);
            allScreens.add(s2);
            allScreens.add(s3);
        }

        // 4. Create Shows for the next 3 days
        LocalDate today = LocalDate.now();
        LocalTime[] startTimes = {
                LocalTime.of(10, 30),
                LocalTime.of(14, 0),
                LocalTime.of(17, 30),
                LocalTime.of(21, 0)
        };

        List<Movie> allMovies = List.of(dune, oppenheimer, spiderman, interstellar, darkKnight, deadpool);

        int movieIdx = 0;
        for (Screen screen : allScreens) {
            Movie movie = allMovies.get(movieIdx % allMovies.size());
            movieIdx++;

            for (int dayOffset = 0; dayOffset < 3; dayOffset++) {
                LocalDate showDate = today.plusDays(dayOffset);
                for (LocalTime time : startTimes) {
                    Show show = new Show();
                    show.setMovie(movie);
                    show.setScreen(screen);
                    show.setShowDate(showDate);
                    show.setStartTime(time);
                    show.setEndTime(time.plusMinutes(movie.getDuration() + 20));
                    show.setPriceRegular(BigDecimal.valueOf(220.00));
                    show.setPricePremium(BigDecimal.valueOf(350.00));
                    show.setPriceRecliner(BigDecimal.valueOf(550.00));
                    showRepository.save(show);
                }
            }
        }

        log.info("CineWave data seeding complete: {} movies, {} theaters, {} screens initialized.",
                allMovies.size(), theaters.size(), allScreens.size());
    }

    private Screen createScreenWithSeats(Theater theater, String screenName) {
        Screen screen = new Screen();
        screen.setTheater(theater);
        screen.setName(screenName);
        screen = screenRepository.save(screen);

        List<Seat> seats = new ArrayList<>();

        // Row A & B: Recliner (6 seats per row)
        for (String row : List.of("A", "B")) {
            for (int num = 1; num <= 6; num++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setRowLabel(row);
                seat.setSeatNumber(num);
                seat.setCategory(SeatCategory.RECLINER);
                seats.add(seat);
            }
        }

        // Row C, D, E: Premium (8 seats per row)
        for (String row : List.of("C", "D", "E")) {
            for (int num = 1; num <= 8; num++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setRowLabel(row);
                seat.setSeatNumber(num);
                seat.setCategory(SeatCategory.PREMIUM);
                seats.add(seat);
            }
        }

        // Row F, G: Regular (8 seats per row)
        for (String row : List.of("F", "G")) {
            for (int num = 1; num <= 8; num++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setRowLabel(row);
                seat.setSeatNumber(num);
                seat.setCategory(SeatCategory.REGULAR);
                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
        return screen;
    }

    private void seedStreams() {
        log.info("Seeding CineWave Streams catalog...");
        List<StreamContent> streams = List.of(
            createStream("The Last Frontier: Arctic Echoes",
                "A breathtaking documentary journey through the pristine glaciers and wildlife of the remote Svalbard archipelago.",
                "Documentary", "English", 92, "U", 0.0,
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1600&q=80",
                "https://www.youtube.com/watch?v=1La4QzGeaaQ"),
            createStream("Shadows in the Mist: Nordic Noir",
                "A tense detective mystery set in a snowbound Scandinavian town where ancient folklore meets cold-blooded modern crime.",
                "Exclusive", "English, Swedish", 118, "A", 149.0,
                "https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1600&q=80",
                "https://www.youtube.com/watch?v=Way9Dexny3w"),
            createStream("Soundscapes: Symphony of Earth",
                "An auditory and visual masterpiece exploring the rhythmic acoustics of rainforests, oceans, and urban wonderlands.",
                "Indie Spotlight", "Instrumental", 75, "U", 0.0,
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=1600&q=80",
                "https://www.youtube.com/watch?v=cqGjhVJWtEg"),
            createStream("Cyber Samurai: Neon Legacy",
                "In 2088 Neo-Kyoto, a lone cybernetically enhanced ronin defends underground rebels against an omnipotent megacorp.",
                "Premiere", "Japanese, English", 130, "UA", 199.0,
                "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1600&q=80",
                "https://www.youtube.com/watch?v=uYPbbksJxIg"),
            createStream("Cinema Paradiso: The Lost Frames",
                "A tender tribute to celluloid film reels, forgotten projection booths, and the timeless magic of indie picture houses.",
                "Film Festival", "Italian, English", 105, "U", 99.0,
                "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&w=1600&q=80",
                "https://www.youtube.com/watch?v=Way9Dexny3w")
        );
        streamContentRepository.saveAll(streams);
        log.info("Seeded {} streams.", streams.size());
    }

    private StreamContent createStream(String title, String desc, String cat, String lang, int dur, String rating, double price, String thumb, String backdrop, String vid) {
        StreamContent s = new StreamContent();
        s.setTitle(title);
        s.setDescription(desc);
        s.setCategory(cat);
        s.setLanguage(lang);
        s.setDuration(dur);
        s.setRating(rating);
        s.setPrice(price);
        s.setThumbnailUrl(thumb);
        s.setBackdropUrl(backdrop);
        s.setVideoUrl(vid);
        s.setActive(true);
        s.setReleaseDate(LocalDate.now().minusWeeks(2));
        return s;
    }

    private void seedEvents() {
        log.info("Seeding CineWave Events catalog...");
        List<Event> events = List.of(
            createEvent("Sunburn Arena ft. Alan Walker Live",
                "Experience the world-renowned electronic producer Alan Walker live with high-energy visuals, cutting-edge stage effects, and timeless anthem tracks.",
                "Music Concert", "Manpho Convention Centre", "Bengaluru",
                LocalDate.now().plusDays(12), LocalTime.of(18, 30), 1499.0, 500,
                "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80"),
            createEvent("Zakir Khan - Tathastu Live Comedy",
                "India's beloved 'Sakht Launda' returns to the stage with his iconic narrative comedy, honest musings on family, romance, and growing up.",
                "Standup Comedy", "NCPA: Tata Theatre", "Mumbai",
                LocalDate.now().plusDays(8), LocalTime.of(20, 0), 999.0, 350,
                "https://images.unsplash.com/photo-1585699324551-f6c309eedeca?auto=format&fit=crop&w=800&q=80"),
            createEvent("Arijit Singh Symphony Night",
                "A soul-stirring live symphonic concert featuring Arijit Singh accompanied by a 45-piece live grand orchestra.",
                "Music Concert", "JLN Stadium Arena", "Delhi NCR",
                LocalDate.now().plusDays(20), LocalTime.of(19, 0), 1999.0, 800,
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80"),
            createEvent("The Broadway Gala: Phantom & Beyond",
                "A mesmerizing theatrical production celebrating Broadway's greatest stage hits, exquisite costumes, and timeless vocal performances.",
                "Theater & Arts", "Shilpakala Vedika", "Hyderabad",
                LocalDate.now().plusDays(15), LocalTime.of(17, 30), 799.0, 400,
                "https://images.unsplash.com/photo-1460723237483-7a6dc9d0b212?auto=format&fit=crop&w=800&q=80")
        );
        eventRepository.saveAll(events);
        log.info("Seeded {} events.", events.size());
    }

    private Event createEvent(String title, String desc, String cat, String venue, String city, LocalDate date, LocalTime time, double price, int tix, String banner) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription(desc);
        e.setCategory(cat);
        e.setVenue(venue);
        e.setCity(city);
        e.setEventDate(date);
        e.setEventTime(time);
        e.setPrice(price);
        e.setTotalTickets(tix);
        e.setAvailableTickets(tix);
        e.setBannerUrl(banner);
        e.setActive(true);
        return e;
    }

    private void seedOffers() {
        log.info("Seeding CineWave Offers catalog...");
        List<Offer> offers = List.of(
            createOffer("CINEWAVE50", "Flat ₹50 OFF on First Booking",
                "Get flat ₹50 instant discount on your first booking with CineWave. Valid across all multiplexes and shows.",
                "FLAT", 50.0, 50.0, 200.0, 1000),
            createOffer("BLOCKBUSTER20", "20% OFF on Weekend Shows",
                "Enjoy 20% discount up to ₹150 on your favorite blockbuster movie tickets during weekends.",
                "PERCENTAGE", 20.0, 150.0, 300.0, 500),
            createOffer("PREMIUM100", "Flat ₹100 OFF on Recliner / IMAX Bookings",
                "Upgrade your cinematic luxury! Flat ₹100 instant discount on Recliner and IMAX ticket reservations.",
                "FLAT", 100.0, 100.0, 450.0, 300),
            createOffer("MOVIEBUFF", "15% OFF across All Multiplexes",
                "Get 15% discount up to ₹120 when booking 2 or more tickets across any participating theater.",
                "PERCENTAGE", 15.0, 120.0, 250.0, 800)
        );
        offerRepository.saveAll(offers);
        log.info("Seeded {} offers.", offers.size());
    }

    private Offer createOffer(String code, String title, String desc, String type, double val, double maxDisc, double minOrder, int limit) {
        Offer o = new Offer();
        o.setCode(code);
        o.setTitle(title);
        o.setDescription(desc);
        o.setDiscountType(type);
        o.setDiscountValue(val);
        o.setMaxDiscount(maxDisc);
        o.setMinOrderAmount(minOrder);
        o.setStartDate(LocalDate.now().minusMonths(1));
        o.setEndDate(LocalDate.now().plusMonths(6));
        o.setUsageLimit(limit);
        o.setUsedCount(0);
        o.setActive(true);
        return o;
    }

    private void ensureUpcomingShowsForAllMovies() {
        LocalDate today = LocalDate.now();
        long upcomingCount = showRepository.countByActiveTrueAndShowDateGreaterThanEqual(today);
        if (upcomingCount >= 60) {
            log.info("Sufficient upcoming shows found ({}), skipping automated showtime generator.", upcomingCount);
            return;
        }

        log.info("Generating comprehensive upcoming shows and verified showtimes for all catalog movies...");

        // Ensure every theater has at least 2 screens with full seat layouts
        List<Theater> allTheaters = theaterRepository.findAll();
        for (Theater t : allTheaters) {
            List<Screen> screens = screenRepository.findByTheaterId(t.getId());
            if (screens.isEmpty()) {
                createScreenWithSeats(t, "Screen 1 - 4K Laser Atmos");
                createScreenWithSeats(t, "Screen 2 - 2K Digital");
            }
        }

        List<Screen> allScreens = screenRepository.findAll();
        List<Movie> allMovies = movieRepository.findByActiveTrue();
        if (allScreens.isEmpty() || allMovies.isEmpty()) {
            return;
        }

        // Standard Multiplex Daily Showtimes
        LocalTime[] dailyShowTimes = {
            LocalTime.of(10, 0),   // Morning Show: 10:00 AM
            LocalTime.of(13, 30),  // Matinee Show: 01:30 PM
            LocalTime.of(17, 0),   // First Show:   05:00 PM
            LocalTime.of(20, 30),  // Second Show:  08:30 PM
            LocalTime.of(23, 45)   // Late Night:   11:45 PM
        };

        int movieIndex = 0;
        int newShowsCreated = 0;

        for (Screen screen : allScreens) {
            Movie movie = allMovies.get(movieIndex % allMovies.size());
            movieIndex++;

            int durationMinutes = movie.getDuration() != null ? movie.getDuration() : 135;

            // Generate shows for today + next 6 days (7 full days)
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate showDate = today.plusDays(dayOffset);

                for (LocalTime time : dailyShowTimes) {
                    LocalTime endTime = time.plusMinutes(durationMinutes + 20);

                    // Overlap guard to prevent duplicates
                    List<Show> existing = showRepository.findOverlappingShows(screen.getId(), showDate, time, endTime);
                    if (existing.isEmpty()) {
                        Show s = new Show();
                        s.setMovie(movie);
                        s.setScreen(screen);
                        s.setShowDate(showDate);
                        s.setStartTime(time);
                        s.setEndTime(endTime);
                        s.setPriceRegular(BigDecimal.valueOf(180.00));
                        s.setPricePremium(BigDecimal.valueOf(280.00));
                        s.setPriceRecliner(BigDecimal.valueOf(450.00));
                        s.setActive(true);
                        showRepository.save(s);
                        newShowsCreated++;
                    }
                }
            }
        }

        log.info("Successfully scheduled {} upcoming shows with verified showtimes across {} auditoriums.",
                newShowsCreated, allScreens.size());
    }
}
