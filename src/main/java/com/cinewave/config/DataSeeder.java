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
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;

    public DataSeeder(
            MovieRepository movieRepository,
            TheaterRepository theaterRepository,
            ScreenRepository screenRepository,
            SeatRepository seatRepository,
            ShowRepository showRepository) {
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (movieRepository.count() > 0) {
            log.info("Database already contains movie data, skipping DataSeeder.");
            return;
        }

        log.info("Populating CineWave with initial catalog: movies, theaters, screens, seats, and shows...");

        // 1. Create Movies
        Movie dune = new Movie();
        dune.setTitle("Dune: Part Two");
        dune.setDescription("Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the universe, he endeavors to prevent a terrible future only he can foresee.");
        dune.setGenre("Sci-Fi / Adventure / Drama");
        dune.setDuration(166);
        dune.setLanguage("English, Hindi");
        dune.setReleaseDate(LocalDate.now().minusDays(15));
        dune.setRating("UA 16+");
        dune.setCastInfo("Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem");
        dune.setPosterUrl("https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80");
        dune.setTrailerUrl("https://www.youtube.com/watch?v=Way9Dexny3w");
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
        oppenheimer.setPosterUrl("https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=800&q=80");
        oppenheimer.setTrailerUrl("https://www.youtube.com/watch?v=uYPbbksJxIg");
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
        spiderman.setPosterUrl("https://images.unsplash.com/photo-1635805737707-575885ab0820?auto=format&fit=crop&w=800&q=80");
        spiderman.setTrailerUrl("https://www.youtube.com/watch?v=cqGjhVJWtEg");
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
        interstellar.setPosterUrl("https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=800&q=80");
        interstellar.setTrailerUrl("https://www.youtube.com/watch?v=zSWdZVtXT7E");
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
        darkKnight.setPosterUrl("https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80");
        darkKnight.setTrailerUrl("https://www.youtube.com/watch?v=EXeTwQWrcwY");
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
        deadpool.setPosterUrl("https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=800&q=80");
        deadpool.setTrailerUrl("https://www.youtube.com/watch?v=73_1biulkYk");
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
}
