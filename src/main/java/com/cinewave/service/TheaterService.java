package com.cinewave.service;

import com.cinewave.dto.*;
import com.cinewave.entity.*;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public TheaterService(TheaterRepository theaterRepository,
                          ScreenRepository screenRepository,
                          SeatRepository seatRepository,
                          UserRepository userRepository,
                          BookingRepository bookingRepository,
                          BookingSeatRepository bookingSeatRepository) {
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    // --- Theater CRUD ---

    public List<TheaterResponse> getAllActiveTheaters() {
        return theaterRepository.findByActiveTrue().stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TheaterResponse> getAllTheaters() {
        return theaterRepository.findAll().stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public TheaterResponse getTheaterById(Long id) {
        Theater t = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + id));
        return TheaterResponse.fromEntity(t);
    }

    public List<TheaterResponse> getTheatersByCity(String city) {
        return theaterRepository.findByCityAndActiveTrue(city).stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TheaterResponse> getTheatersByState(String state) {
        return theaterRepository.findByStateAndActiveTrue(state).stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TheaterResponse> getVerifiedTheaters() {
        return theaterRepository.findByVerifiedPartnerTrueAndActiveTrue().stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TheaterResponse> getVerifiedTheatersByCity(String city) {
        return theaterRepository.findByCityIgnoreCaseAndVerifiedPartnerTrueAndActiveTrue(city).stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TheaterResponse> searchTheaters(String query) {
        if (query == null || query.isBlank()) {
            return getAllActiveTheaters();
        }
        return theaterRepository.searchTheaters(query.trim()).stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TheaterResponse> getTheatersByOwner(Long ownerId) {
        return theaterRepository.findByOwnerIdAndActiveTrue(ownerId).stream()
                .map(TheaterResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<String> getCities() {
        return theaterRepository.findDistinctCities();
    }

    public List<String> getStates() {
        return theaterRepository.findDistinctStates();
    }

    public List<String> getDistrictsByState(String state) {
        return theaterRepository.findDistinctDistrictsByState(state);
    }

    public List<String> getCitiesByState(String state) {
        return theaterRepository.findDistinctCitiesByState(state);
    }

    public TheaterResponse createTheater(TheaterRequest request) {
        String normalizedKey = TheaterDirectoryService.buildNormalizedKey(request.getName(), request.getCity());
        if (theaterRepository.existsByNormalizedKey(normalizedKey)) {
            throw new BadRequestException("A cinema named '" + request.getName().trim() + "' in " + request.getCity().trim() + " is already registered.");
        }

        Theater t = new Theater();
        t.setName(request.getName().trim());
        t.setCity(request.getCity().trim());
        if (request.getState() != null && !request.getState().isBlank()) {
            t.setState(request.getState().trim());
        }
        if (request.getDistrict() != null && !request.getDistrict().isBlank()) {
            t.setDistrict(request.getDistrict().trim());
        }
        t.setAddress(request.getAddress() != null ? request.getAddress().trim() : "");
        t.setFacilities(request.getFacilities() != null ? request.getFacilities().trim() : "");
        t.setProviderId(request.getProviderId());
        t.setDiscoverySource(request.getDiscoverySource() != null ? request.getDiscoverySource() : "MANUAL");
        t.setApprovalStatus(request.getApprovalStatus() != null ? request.getApprovalStatus() : "APPROVED");
        t.setVerifiedPartner(request.getVerifiedPartner() != null ? request.getVerifiedPartner() : true);
        t.setLatitude(request.getLatitude());
        t.setLongitude(request.getLongitude());
        t.setPhone(request.getPhone());
        t.setPincode(request.getPincode());
        t.setWebsite(request.getWebsite());
        t.setNormalizedKey(normalizedKey);

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + request.getOwnerId()));
            t.setOwner(owner);
        }
        t = theaterRepository.save(t);
        return TheaterResponse.fromEntity(t);
    }

    public TheaterResponse updateTheater(Long id, TheaterRequest request) {
        Theater t = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + id));
        t.setName(request.getName());
        t.setCity(request.getCity());
        if (request.getState() != null && !request.getState().isBlank()) {
            t.setState(request.getState());
        }
        if (request.getDistrict() != null) {
            t.setDistrict(request.getDistrict());
        }
        t.setAddress(request.getAddress());
        t.setFacilities(request.getFacilities());
        if (request.getProviderId() != null) t.setProviderId(request.getProviderId());
        if (request.getDiscoverySource() != null) t.setDiscoverySource(request.getDiscoverySource());
        if (request.getApprovalStatus() != null) t.setApprovalStatus(request.getApprovalStatus());
        if (request.getVerifiedPartner() != null) t.setVerifiedPartner(request.getVerifiedPartner());
        if (request.getLatitude() != null) t.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) t.setLongitude(request.getLongitude());
        if (request.getPhone() != null) t.setPhone(request.getPhone());
        if (request.getPincode() != null) t.setPincode(request.getPincode());
        if (request.getWebsite() != null) t.setWebsite(request.getWebsite());
        t.setNormalizedKey(TheaterDirectoryService.buildNormalizedKey(request.getName(), request.getCity()));

        if (request.getOwnerId() != null) {
            User owner = userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + request.getOwnerId()));
            t.setOwner(owner);
        } else {
            t.setOwner(null);
        }
        t = theaterRepository.save(t);
        return TheaterResponse.fromEntity(t);
    }

    public TheaterResponse assignOwner(Long theaterId, Long ownerId) {
        Theater t = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + theaterId));
        if (ownerId != null) {
            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Owner user not found: " + ownerId));
            t.setOwner(owner);
        } else {
            t.setOwner(null);
        }
        t = theaterRepository.save(t);
        return TheaterResponse.fromEntity(t);
    }

    public TheaterResponse toggleTheaterStatus(Long id) {
        Theater t = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + id));
        if (t.isActive()) {
            long futureBookings = bookingRepository.countByShowScreenTheaterIdAndStatusAndShowShowDateGreaterThanEqual(
                    id, BookingStatus.CONFIRMED, LocalDate.now());
            if (futureBookings > 0) {
                throw new BadRequestException("Cannot deactivate theater: has " + futureBookings + " confirmed bookings for upcoming shows");
            }
        }
        t.setActive(!t.isActive());
        t = theaterRepository.save(t);
        return TheaterResponse.fromEntity(t);
    }

    public void deleteTheater(Long id) {
        Theater t = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + id));
        long futureBookings = bookingRepository.countByShowScreenTheaterIdAndStatusAndShowShowDateGreaterThanEqual(
                id, BookingStatus.CONFIRMED, LocalDate.now());
        if (futureBookings > 0) {
            throw new BadRequestException("Cannot delete theater: has " + futureBookings + " confirmed bookings for upcoming shows. Please cancel or complete shows first.");
        }
        // Soft delete to protect booking records
        t.setActive(false);
        theaterRepository.save(t);
    }

    // --- Screen CRUD ---

    public List<ScreenResponse> getScreensByTheater(Long theaterId) {
        return screenRepository.findByTheaterId(theaterId).stream()
                .map(ScreenResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ScreenResponse getScreenById(Long id) {
        Screen s = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + id));
        return ScreenResponse.fromEntityWithSeats(s);
    }

    public ScreenResponse createScreen(ScreenRequest request) {
        Theater t = theaterRepository.findById(request.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + request.getTheaterId()));
        Screen s = new Screen();
        s.setName(request.getName());
        s.setTheater(t);
        s.setActive(true);
        s = screenRepository.save(s);
        return ScreenResponse.fromEntity(s);
    }

    public ScreenResponse updateScreen(Long id, ScreenUpdateRequest request) {
        Screen s = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + id));
        if (request.getName() != null && !request.getName().isBlank()) {
            s.setName(request.getName());
        }
        if (request.getActive() != null) {
            if (!request.getActive()) {
                long futureBookings = bookingRepository.countByShowScreenIdAndStatusAndShowShowDateGreaterThanEqual(
                        id, BookingStatus.CONFIRMED, LocalDate.now());
                if (futureBookings > 0) {
                    throw new BadRequestException("Cannot deactivate screen: has " + futureBookings + " active bookings for upcoming shows");
                }
            }
            s.setActive(request.getActive());
        }
        s = screenRepository.save(s);
        return ScreenResponse.fromEntity(s);
    }

    public void deleteScreen(Long id) {
        Screen s = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + id));
        long futureBookings = bookingRepository.countByShowScreenIdAndStatusAndShowShowDateGreaterThanEqual(
                id, BookingStatus.CONFIRMED, LocalDate.now());
        if (futureBookings > 0) {
            throw new BadRequestException("Cannot delete screen: has " + futureBookings + " active bookings for upcoming shows");
        }
        s.setActive(false);
        screenRepository.save(s);
    }

    // --- Seat Management ---

    public List<SeatResponse> getSeatsByScreen(Long screenId) {
        return seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screenId).stream()
                .map(SeatResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SeatResponse getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + id));
        return SeatResponse.fromEntity(seat);
    }

    public SeatResponse createSeat(SeatRequest request) {
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));

        // Prevent duplicate seat within the screen
        List<Seat> existing = seatRepository.findByScreenId(screen.getId());
        boolean duplicate = existing.stream().anyMatch(s ->
                s.getRowLabel().equalsIgnoreCase(request.getRowLabel()) &&
                s.getSeatNumber().equals(request.getSeatNumber()));
        if (duplicate) {
            throw new BadRequestException("Seat " + request.getRowLabel() + request.getSeatNumber() + " already exists on this screen");
        }

        Seat seat = new Seat(request.getRowLabel(), request.getSeatNumber(),
                             request.getCategory(), screen);
        seat = seatRepository.save(seat);
        return SeatResponse.fromEntity(seat);
    }

    @Transactional
    public List<SeatResponse> createBulkSeats(Long screenId, List<SeatRequest> requests) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + screenId));

        List<Seat> existing = seatRepository.findByScreenId(screenId);

        List<Seat> seatsToSave = requests.stream().filter(req -> {
            boolean duplicate = existing.stream().anyMatch(s ->
                    s.getRowLabel().equalsIgnoreCase(req.getRowLabel()) &&
                    s.getSeatNumber().equals(req.getSeatNumber()));
            return !duplicate;
        }).map(req -> new Seat(req.getRowLabel(), req.getSeatNumber(), req.getCategory(), screen))
          .collect(Collectors.toList());

        List<Seat> saved = seatRepository.saveAll(seatsToSave);
        return saved.stream().map(SeatResponse::fromEntity).collect(Collectors.toList());
    }

    public SeatResponse updateSeat(Long seatId, SeatUpdateRequest request) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));

        // Prevent unsafe changes if seat is reserved in upcoming shows
        long futureBookings = bookingSeatRepository.countFutureConfirmedBookingsForSeat(seatId, LocalDate.now());
        if (futureBookings > 0) {
            throw new BadRequestException("Cannot modify seat " + seat.getRowLabel() + seat.getSeatNumber() +
                    ": already reserved in " + futureBookings + " upcoming shows");
        }

        // Check duplicate within the screen
        if (!seat.getRowLabel().equalsIgnoreCase(request.getRowLabel()) ||
            !seat.getSeatNumber().equals(request.getSeatNumber())) {
            List<Seat> existing = seatRepository.findByScreenId(seat.getScreen().getId());
            boolean duplicate = existing.stream().anyMatch(s ->
                    !s.getId().equals(seatId) &&
                    s.getRowLabel().equalsIgnoreCase(request.getRowLabel()) &&
                    s.getSeatNumber().equals(request.getSeatNumber()));
            if (duplicate) {
                throw new BadRequestException("Seat " + request.getRowLabel() + request.getSeatNumber() + " already exists on this screen");
            }
        }

        seat.setRowLabel(request.getRowLabel());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setCategory(request.getCategory());
        seat = seatRepository.save(seat);
        return SeatResponse.fromEntity(seat);
    }

    public void deleteSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));

        long futureBookings = bookingSeatRepository.countFutureConfirmedBookingsForSeat(seatId, LocalDate.now());
        if (futureBookings > 0) {
            throw new BadRequestException("Cannot delete seat " + seat.getRowLabel() + seat.getSeatNumber() +
                    ": already reserved in " + futureBookings + " upcoming shows");
        }

        seatRepository.delete(seat);
    }
}
