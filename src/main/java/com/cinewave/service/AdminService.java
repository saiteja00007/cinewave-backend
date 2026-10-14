package com.cinewave.service;

import com.cinewave.dto.*;
import com.cinewave.entity.BookingStatus;
import com.cinewave.entity.Role;
import com.cinewave.entity.Theater;
import com.cinewave.entity.User;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class AdminService {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(MovieRepository movieRepository,
                        TheaterRepository theaterRepository,
                        UserRepository userRepository,
                        ShowRepository showRepository,
                        BookingRepository bookingRepository,
                        BookingSeatRepository bookingSeatRepository,
                        PasswordEncoder passwordEncoder) {
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        DashboardResponse dash = new DashboardResponse();
        dash.setTotalMovies(movieRepository.countByActiveTrue());
        dash.setTotalTheaters(theaterRepository.countByActiveTrue());
        dash.setTotalUsers(userRepository.count());
        dash.setTotalShows(showRepository.countByActiveTrue());
        dash.setTotalBookings(bookingRepository.count());
        dash.setConfirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        dash.setCancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED));
        dash.setTotalRevenue(bookingRepository.totalRevenue());

        // Recent 10 bookings
        List<BookingResponse> recent = bookingRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .limit(10)
                .map(b -> {
                    b.setBookingSeats(bookingSeatRepository.findByBookingId(b.getId()));
                    return BookingResponse.fromEntity(b);
                })
                .collect(Collectors.toList());
        dash.setRecentBookings(recent);

        return dash;
    }

    // --- Users CRUD ---

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        return userRepository.searchUsers(query).stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setActive(!user.isActive());
        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setRole(newRole);
        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    // --- Owners CRUD ---

    @Transactional(readOnly = true)
    public List<UserResponse> getTheaterOwners() {
        return userRepository.findByRole(Role.THEATER_OWNER).stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OwnerResponse> getAllOwnersWithTheaters() {
        List<User> owners = userRepository.findByRole(Role.THEATER_OWNER);
        List<OwnerResponse> responses = new ArrayList<>();
        for (User owner : owners) {
            List<TheaterResponse> assigned = theaterRepository.findByOwnerIdAndActiveTrue(owner.getId())
                    .stream().map(TheaterResponse::fromEntity).collect(Collectors.toList());
            responses.add(OwnerResponse.fromEntity(owner, assigned));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public OwnerResponse getOwnerById(Long ownerId) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + ownerId));
        if (user.getRole() != Role.THEATER_OWNER) {
            throw new BadRequestException("User is not a theater owner");
        }
        List<TheaterResponse> assigned = theaterRepository.findByOwnerIdAndActiveTrue(user.getId())
                .stream().map(TheaterResponse::fromEntity).collect(Collectors.toList());
        return OwnerResponse.fromEntity(user, assigned);
    }

    public OwnerResponse createOwner(OwnerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        String rawPassword = request.getPassword() != null && !request.getPassword().isBlank()
                ? request.getPassword()
                : "Owner@123";

        User owner = new User();
        owner.setName(request.getName());
        owner.setEmail(request.getEmail().toLowerCase().trim());
        owner.setPassword(passwordEncoder.encode(rawPassword));
        owner.setPhone(request.getPhone());
        owner.setRole(Role.THEATER_OWNER);
        owner.setActive(true);
        owner = userRepository.save(owner);

        // Assign any specified theaters
        List<TheaterResponse> assignedTheaters = new ArrayList<>();
        if (request.getAssignedTheaterIds() != null && !request.getAssignedTheaterIds().isEmpty()) {
            for (Long tId : request.getAssignedTheaterIds()) {
                Theater t = theaterRepository.findById(tId)
                        .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + tId));
                t.setOwner(owner);
                theaterRepository.save(t);
                assignedTheaters.add(TheaterResponse.fromEntity(t));
            }
        }

        return OwnerResponse.fromEntity(owner, assignedTheaters);
    }

    public OwnerResponse updateOwner(Long ownerId, OwnerRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + ownerId));

        if (request.getName() != null && !request.getName().isBlank()) {
            owner.setName(request.getName());
        }
        if (request.getPhone() != null) {
            owner.setPhone(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(owner.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use: " + request.getEmail());
            }
            owner.setEmail(request.getEmail().toLowerCase().trim());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            owner.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        owner = userRepository.save(owner);

        // Update assigned theaters if provided
        if (request.getAssignedTheaterIds() != null) {
            // First unassign any theaters currently assigned to this owner not in the new list
            List<Theater> current = theaterRepository.findByOwnerIdAndActiveTrue(ownerId);
            for (Theater t : current) {
                if (!request.getAssignedTheaterIds().contains(t.getId())) {
                    t.setOwner(null);
                    theaterRepository.save(t);
                }
            }
            // Assign new list
            for (Long tId : request.getAssignedTheaterIds()) {
                Theater t = theaterRepository.findById(tId)
                        .orElseThrow(() -> new ResourceNotFoundException("Theater not found: " + tId));
                t.setOwner(owner);
                theaterRepository.save(t);
            }
        }

        List<TheaterResponse> assignedTheaters = theaterRepository.findByOwnerIdAndActiveTrue(owner.getId())
                .stream().map(TheaterResponse::fromEntity).collect(Collectors.toList());

        return OwnerResponse.fromEntity(owner, assignedTheaters);
    }

    public OwnerResponse toggleOwnerStatus(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + ownerId));
        owner.setActive(!owner.isActive());
        owner = userRepository.save(owner);
        List<TheaterResponse> assignedTheaters = theaterRepository.findByOwnerIdAndActiveTrue(owner.getId())
                .stream().map(TheaterResponse::fromEntity).collect(Collectors.toList());
        return OwnerResponse.fromEntity(owner, assignedTheaters);
    }

    public void deactivateOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + ownerId));
        owner.setActive(false);
        userRepository.save(owner);
    }
}
