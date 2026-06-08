package com.messageforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

public class MessageDtos {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateMessageRequest {
        @NotBlank(message = "Raw content is required")
        private String rawContent;
        
        @Size(max = 255, message = "Title must not exceed 255 characters")
        private String title;

        private String recipient;
        
        private JsonNode metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateMessageRequest {
        @NotBlank(message = "Raw content is required")
        private String rawContent;
        
        @Size(max = 255, message = "Title must not exceed 255 characters")
        private String title;

        private String recipient;
        
        private JsonNode metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        private List<String> channels;
        private String recipient; // The phone number, email, or chat ID of the target
        private boolean testMode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageResponse {
        private UUID id;
        private String rawContent;
        private String title;
        private String recipient;
        private String status;
        private List<ChannelMessageResponse> channelMessages;
        private LocalDateTime createdAt;
        private LocalDateTime sentAt;
        private JsonNode metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChannelMessageResponse {
        private UUID id;
        private String channelType;
        private String formattedContent;
        private int characterCount;
        private String status;
        private String warning;
        private boolean requiresTruncation;
        private String errorMessage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreviewRequest {
        private String rawContent;
        private List<String> activeChannels;
        private JsonNode decorators;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreviewResponse {
        private String channelType;
        private String formattedContent;
        private int characterCount;
        private int characterLimit;
        private String warning;
        private boolean requiresTruncation;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageListResponse {
        private List<MessageResponse> messages;
        private int page;
        private int pageSize;
        private long totalCount;
        private int totalPages;
    }
}
