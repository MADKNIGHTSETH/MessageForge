package com.messageforge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class AuthDtos {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
        
        @NotBlank(message = "Display name is required")
        @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
        private String displayName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserDto user;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        private UUID id;
        private String email;
        private String displayName;
        private String avatarUrl;
        private String role;
        private Boolean isActive;
        private String createdAt;
    }
}
