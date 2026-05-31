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
@Table(name = "channel_integrations", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_enabled", columnList = "is_enabled")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_channel", columnNames = {"user_id", "channel_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelIntegration {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channelType;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEnabled = false;
    
    @Column(columnDefinition = "BYTEA")
    private byte[] credentials;
    
    @Column(columnDefinition = "JSONB")
    @Type(JsonBinaryType.class)
    private JsonNode settings;
    
    @Column
    private LocalDateTime lastTestedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TestStatus testStatus;
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime deletedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TestStatus {
        PENDING, SUCCESS, FAILED
    }
}
