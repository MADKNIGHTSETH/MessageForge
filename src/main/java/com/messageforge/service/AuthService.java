package com.messageforge.service;

import com.messageforge.config.JwtTokenProvider;
import com.messageforge.dto.AuthDtos;
import com.messageforge.model.User;
import com.messageforge.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final long jwtExpiration;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtExpiration = jwtTokenProvider.getAccessTokenExpirationMillis();
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName().trim())
                .isActive(true)
                .build();

        return buildAuthResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    private AuthDtos.AuthResponse buildAuthResponse(User user) {
        return AuthDtos.AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user.getEmail()))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user.getEmail()))
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .user(toUserDto(user))
                .build();
    }

    private AuthDtos.UserDto toUserDto(User user) {
        return AuthDtos.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt() == null ? null : user.getCreatedAt().toString())
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
