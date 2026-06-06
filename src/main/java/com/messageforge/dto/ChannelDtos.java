package com.messageforge.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ChannelDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfigureIntegrationRequest {
        private boolean enabled;
        private JsonNode credentials;
        private JsonNode settings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntegrationResponse {
        private String channelType;
        private String displayName;
        private boolean enabled;
        private JsonNode settings;
        private LocalDateTime lastTestedAt;
        private String testStatus;
    }
}
