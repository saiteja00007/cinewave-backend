package com.cinewave.service;

import com.cinewave.dto.AuthResponse;
import com.cinewave.dto.LoginRequest;
import com.cinewave.dto.RegisterRequest;
import com.cinewave.dto.UserResponse;
import com.cinewave.entity.Role;
import com.cinewave.entity.User;
import com.cinewave.exception.BadRequestException;
import com.cinewave.exception.ConflictException;
import com.cinewave.repository.UserRepository;
import com.cinewave.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice", "alice@example.com", "encodedPass", Role.USER);
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("Password@123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateToken("alice@example.com", "USER")).thenReturn("mock-jwt-token");

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals("alice@example.com", res.getUser().getEmail());
        assertEquals("Alice", res.getUser().getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsConflictException() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("Password@123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("Password@123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateToken("alice@example.com", "USER")).thenReturn("mock-jwt-token");

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals("alice@example.com", res.getUser().getEmail());
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_BadCredentials_ThrowsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("WrongPassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void getCurrentUser_Success() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));

        UserResponse res = authService.getCurrentUser("alice@example.com");
        assertNotNull(res);
        assertEquals("alice@example.com", res.getEmail());
    }

    @Test
    void getCurrentUser_NotFound_ThrowsBadRequestException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.getCurrentUser("unknown@example.com"));
    }
}
