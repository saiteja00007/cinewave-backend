package com.cinewave.service;

import com.cinewave.dto.ShowRequest;
import com.cinewave.dto.ShowResponse;
import com.cinewave.entity.*;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public ShowService(ShowRepository showRepository,
                       MovieRepository movieRepository,
                       ScreenRepository screenRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
    }

    public List<ShowResponse> getAllUpcomingShows() {
        return showRepository.findAllUpcoming(LocalDate.now()).stream()
                .map(ShowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ShowResponse getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));
        return ShowResponse.fromEntity(show);
    }

    public List<ShowResponse> getShowsByMovie(Long movieId) {
        return showRepository.findUpcomingShowsByMovie(movieId, LocalDate.now()).stream()
                .map(ShowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ShowResponse createShow(ShowRequest request) {
        // Validate movie
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + request.getMovieId()));
        if (!movie.isActive()) {
            throw new BadRequestException("Cannot schedule show for inactive movie");
        }

        // Validate screen
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));

        // Auto-calculate end time if not specified: movie duration + 20 min interval
        LocalTime endTime = request.getEndTime();
        if (endTime == null) {
            int durationMinutes = movie.getDuration() != null ? movie.getDuration() : 120;
            endTime = request.getStartTime().plusMinutes(durationMinutes + 20);
        }

        // Validate times
        if (endTime.isBefore(request.getStartTime()) || endTime.equals(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Check for past shows
        if (request.getShowDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot schedule shows in the past");
        }

        // Check for overlapping shows on the same screen
        List<Show> overlapping = showRepository.findOverlappingShows(
                request.getScreenId(), request.getShowDate(),
                request.getStartTime(), endTime);
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Show overlaps with an existing show on this screen");
        }

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(endTime);
        show.setPriceRegular(request.getPriceRegular());
        show.setPricePremium(request.getPricePremium());
        show.setPriceRecliner(request.getPriceRecliner());

        show = showRepository.save(show);
        return ShowResponse.fromEntity(show);
    }

    public ShowResponse updateShow(Long id, ShowRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + request.getMovieId()));
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));

        LocalTime endTime = request.getEndTime();
        if (endTime == null) {
            int durationMinutes = movie.getDuration() != null ? movie.getDuration() : 120;
            endTime = request.getStartTime().plusMinutes(durationMinutes + 20);
        }

        if (endTime.isBefore(request.getStartTime()) || endTime.equals(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Check overlaps excluding this show
        List<Show> overlapping = showRepository.findOverlappingShows(
                request.getScreenId(), request.getShowDate(),
                request.getStartTime(), endTime);
        overlapping.removeIf(s -> s.getId().equals(id));
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Show overlaps with an existing show on this screen");
        }

        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(endTime);
        show.setPriceRegular(request.getPriceRegular());
        show.setPricePremium(request.getPricePremium());
        show.setPriceRecliner(request.getPriceRecliner());

        show = showRepository.save(show);
        return ShowResponse.fromEntity(show);
    }

    public void deleteShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + id));
        show.setActive(false);
        showRepository.save(show);
    }
}
