package com.messageforge.service;

import com.messageforge.dto.AdminDtos.*;
import com.messageforge.dto.AuthDtos.UserDto;
import com.messageforge.exception.ApiException;
import com.messageforge.model.AuditLog;
import com.messageforge.model.User;
import com.messageforge.repository.AuditLogRepository;
import com.messageforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuthService authService;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto toggleUserStatus(UUID userId, boolean active, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        user.setIsActive(active);
        User saved = userRepository.save(user);

        logAudit(admin, active ? "USER_ACTIVATED" : "USER_DEACTIVATED", "User", userId.toString(), null);
        
        log.info("Admin {} {} user {}", admin.getEmail(), active ? "activated" : "deactivated", user.getEmail());
        return authService.mapToUserDto(saved);
    }

    public List<AuditLogResponse> getAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void logAudit(User user, String action, String entityType, String entityId, com.fasterxml.jackson.databind.JsonNode details) {
        AuditLog logEntry = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .changes(details)
                .build();
        auditLogRepository.save(logEntry);
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .userEmail(log.getUser().getEmail())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getChanges())
                .createdAt(log.getCreatedAt().toString())
                .build();
    }
}
