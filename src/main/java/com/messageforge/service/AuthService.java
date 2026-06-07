package com.messageforge.service;

import com.messageforge.config.JwtTokenProvider;
import com.messageforge.dto.AuthDtos.*;
import com.messageforge.exception.ApiException;
import com.messageforge.model.RefreshToken;
import com.messageforge.model.User;
import com.messageforge.repository.RefreshTokenRepository;
import com.messageforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email is already registered", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user: {}", savedUser.getEmail());

        return mapToUserDto(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // Delete existing token if any
        refreshTokenRepository.deleteByUserId(user.getId());

        // Create new RefreshToken in DB
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenString)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 days expiration
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Successfully authenticated user: {}", user.getEmail());

        long expiresIn = jwtTokenProvider.getExpirationTime(accessToken) / 1000;
        if (expiresIn <= 0) {
            expiresIn = 900; // default 15 min if parse fails
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenStr = request.getRefreshToken();
        
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenStr)
                .orElseThrow(() -> new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (refreshToken.getRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new ApiException("Expired or revoked refresh token", HttpStatus.UNAUTHORIZED);
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        
        long expiresIn = jwtTokenProvider.getExpirationTime(newAccessToken) / 1000;
        if (expiresIn <= 0) {
            expiresIn = 900; // default 15 min
        }

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(tokenStr)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public void logout(User user) {
        if (user != null) {
            refreshTokenRepository.deleteByUserId(user.getId());
            log.info("Successfully logged out user: {}", user.getEmail());
        }
    }

    public UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}
