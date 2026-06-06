package com.messageforge.repository;

import com.messageforge.model.ChannelIntegration;
import com.messageforge.model.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelIntegrationRepository extends JpaRepository<ChannelIntegration, UUID> {
    
    Optional<ChannelIntegration> findByUserIdAndChannelType(UUID userId, ChannelType channelType);
    
    List<ChannelIntegration> findByUserIdAndDeletedAtIsNull(UUID userId);
    
    @Query("SELECT ci FROM ChannelIntegration ci WHERE ci.user.id = :userId AND ci.isEnabled = true AND ci.deletedAt IS NULL")
    List<ChannelIntegration> findEnabledIntegrationsByUserId(UUID userId);
    
    @Query("SELECT ci FROM ChannelIntegration ci WHERE ci.user.id = :userId AND ci.channelType = :channelType AND ci.deletedAt IS NULL")
    Optional<ChannelIntegration> findByUserIdAndChannelTypeNotDeleted(UUID userId, ChannelType channelType);
    
    @Query("SELECT COUNT(ci) FROM ChannelIntegration ci WHERE ci.user.id = :userId AND ci.isEnabled = true")
    int countEnabledChannelsByUserId(UUID userId);
}
