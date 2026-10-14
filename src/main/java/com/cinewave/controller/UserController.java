package com.cinewave.controller;

import com.cinewave.dto.BookingResponse;
import com.cinewave.dto.UpdateProfileRequest;
import com.cinewave.dto.UserResponse;
import com.cinewave.entity.User;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ResourceNotFoundException;
import com.cinewave.repository.UserRepository;
import com.cinewave.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Patron User Account & Self-Service Management Endpoints.
 * Users can only read and manage their own profile, bookings, and account status.
 */
@RestController
@RequestMapping("/api/user")
@PreAuthorize("isAuthenticated()")
@SuppressWarnings("null")
public class UserController {

    private final UserRepository userRepository;
    private final BookingService bookingService;

    public UserController(UserRepository userRepository, BookingService bookingService) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new BadRequestException("Unauthenticated request");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    // --- Profile Management ---

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        user = userRepository.save(user);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/deactivate")
    public ResponseEntity<Map<String, String>> deactivateAccount(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Your account has been deactivated successfully."));
    }

    // --- User Bookings & Cancellation ---

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        return ResponseEntity.ok(bookingService.getUserBookings(user.getEmail()));
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            Authentication auth) {
        User user = getAuthenticatedUser(auth);
        return ResponseEntity.ok(bookingService.cancelBooking(id, user.getEmail()));
    }
}
