package com.messageforge.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AdminDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditLogResponse {
        private UUID id;
        private UUID userId;
        private String userEmail;
        private String action;
        private String entityType;
        private String entityId;
        private JsonNode details;
        private String createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToggleUserStatusRequest {
        private boolean active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChannelStats {
        private String channel;
        private long sent;
        private long failed;
        private long pending;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GlobalStatsResponse {
        private long totalUsers;
        private long totalMessages;
        private List<ChannelStats> channelStats;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateRequest {
        private String channelType;
        private String name;
        private String templateBody;
        private boolean isDefault;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateResponse {
        private UUID id;
        private String channelType;
        private String name;
        private String templateBody;
        private boolean isDefault;
        private String createdAt;
    }
}
