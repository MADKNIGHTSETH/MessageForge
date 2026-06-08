package com.messageforge.service;

import com.messageforge.config.JwtTokenProvider;
import com.messageforge.dto.AuthDtos.*;
import com.messageforge.exception.ApiException;
import com.messageforge.model.RefreshToken;
import com.messageforge.model.User;
import com.messageforge.repository.RefreshTokenRepository;
import com.messageforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .displayName("Test User")
                .role("USER")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        registerRequest = new RegisterRequest("test@example.com", "password123", "Test User");
        loginRequest = new LoginRequest("test@example.com", "password123");
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getDisplayName(), result.getDisplayName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.register(registerRequest);
        });
        assertEquals("Email is already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationTime(anyString())).thenReturn(900000L);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
        
        verify(refreshTokenRepository).deleteByUserId(testUser.getId());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        // Arrange
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void updateProfile_Success() {
        // Arrange
        UpdateProfileRequest updateRequest = new UpdateProfileRequest("New Name", "http://avatar.com/1.png");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = authService.updateProfile(testUser, updateRequest);

        // Assert
        assertEquals("New Name", result.getDisplayName());
        assertEquals("http://avatar.com/1.png", result.getAvatarUrl());
        verify(userRepository).save(testUser);
    }
}
