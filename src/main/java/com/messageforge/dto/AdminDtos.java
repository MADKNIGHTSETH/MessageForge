package com.messageforge.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
}
