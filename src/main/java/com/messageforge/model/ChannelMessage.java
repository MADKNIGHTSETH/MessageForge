package com.messageforge.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "channel_messages", indexes = {
    @Index(name = "idx_message_id", columnList = "message_id"),
    @Index(name = "idx_channel_type", columnList = "channel_type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMessage {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channelType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String formattedContent;
    
    @Column(columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private JsonNode appliedDecorators;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChannelMessageStatus status = ChannelMessageStatus.PENDING;
    
    @Column
    private LocalDateTime sentAt;
    
    @Column(length = 255)
    private String externalMessageId;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ChannelMessageStatus {
        PENDING, SENDING, SENT, FAILED, BOUNCED, DELIVERED
    }
}
