package com.messageforge.service;

import com.messageforge.dto.AdminDtos.*;
import com.messageforge.dto.AuthDtos.UserDto;
import com.messageforge.exception.ApiException;
import com.messageforge.model.AuditLog;
import com.messageforge.model.User;
import com.messageforge.model.ChannelType;
import com.messageforge.model.ChannelMessage.ChannelMessageStatus;
import com.messageforge.repository.AuditLogRepository;
import com.messageforge.repository.UserRepository;
import com.messageforge.repository.MessageRepository;
import com.messageforge.repository.ChannelMessageRepository;
import com.messageforge.model.FormatterTemplate;
import com.messageforge.repository.FormatterTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final MessageRepository messageRepository;
    private final ChannelMessageRepository channelMessageRepository;
    private final FormatterTemplateRepository templateRepository;
    private final AuthService authService;

    public List<TemplateResponse> getSystemTemplates() {
        return templateRepository.findByIsSystemTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToTemplateResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TemplateResponse createSystemTemplate(TemplateRequest request, User admin) {
        FormatterTemplate template = FormatterTemplate.builder()
                .user(admin)
                .channelType(ChannelType.fromString(request.getChannelType()))
                .name(request.getName())
                .templateBody(request.getTemplateBody())
                .isDefault(request.isDefault())
                .isSystem(true)
                .build();

        if (request.isDefault()) {
            // Unset default for others of same channel
            unsetDefaultsForChannel(template.getChannelType());
        }

        FormatterTemplate saved = templateRepository.save(template);
        logAudit(admin, "TEMPLATE_CREATED", "FormatterTemplate", saved.getId().toString(), null);
        return mapToTemplateResponse(saved);
    }

    @Transactional
    public TemplateResponse updateSystemTemplate(UUID id, TemplateRequest request, User admin) {
        FormatterTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ApiException("Template not found", HttpStatus.NOT_FOUND));

        template.setChannelType(ChannelType.fromString(request.getChannelType()));
        template.setName(request.getName());
        template.setTemplateBody(request.getTemplateBody());

        if (request.isDefault() && !template.getIsDefault()) {
            unsetDefaultsForChannel(template.getChannelType());
        }
        template.setIsDefault(request.isDefault());

        FormatterTemplate saved = templateRepository.save(template);
        logAudit(admin, "TEMPLATE_UPDATED", "FormatterTemplate", saved.getId().toString(), null);
        return mapToTemplateResponse(saved);
    }

    @Transactional
    public void deleteSystemTemplate(UUID id, User admin) {
        FormatterTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ApiException("Template not found", HttpStatus.NOT_FOUND));
        
        templateRepository.delete(template);
        logAudit(admin, "TEMPLATE_DELETED", "FormatterTemplate", id.toString(), null);
    }

    private void unsetDefaultsForChannel(ChannelType type) {
        List<FormatterTemplate> existing = templateRepository.findByIsSystemTrueOrderByCreatedAtDesc();
        for (FormatterTemplate t : existing) {
            if (t.getChannelType() == type && t.getIsDefault()) {
                t.setIsDefault(false);
                templateRepository.save(t);
            }
        }
    }

    private TemplateResponse mapToTemplateResponse(FormatterTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .channelType(template.getChannelType().name())
                .name(template.getName())
                .templateBody(template.getTemplateBody())
                .isDefault(template.getIsDefault())
                .createdAt(template.getCreatedAt().toString())
                .build();
    }

    public GlobalStatsResponse getGlobalStats() {
        long totalUsers = userRepository.count();
        long totalMessages = messageRepository.count();

        List<Object[]> rawStats = channelMessageRepository.getChannelMessageStats();
        Map<String, ChannelStats> statsMap = new HashMap<>();

        for (Object[] row : rawStats) {
            ChannelType type = (ChannelType) row[0];
            ChannelMessageStatus status = (ChannelMessageStatus) row[1];
            long count = (Long) row[2];

            String channelName = type.name();
            statsMap.putIfAbsent(channelName, new ChannelStats(channelName, 0, 0, 0));
            ChannelStats stats = statsMap.get(channelName);

            if (status == ChannelMessageStatus.SENT || status == ChannelMessageStatus.DELIVERED) {
                stats.setSent(stats.getSent() + count);
            } else if (status == ChannelMessageStatus.FAILED || status == ChannelMessageStatus.BOUNCED) {
                stats.setFailed(stats.getFailed() + count);
            } else {
                stats.setPending(stats.getPending() + count);
            }
        }

        return GlobalStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalMessages(totalMessages)
                .channelStats(statsMap.values().stream().collect(Collectors.toList()))
                .build();
    }

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
