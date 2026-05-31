package com.messageforge.repository;

import com.messageforge.model.ChannelMessage;
import com.messageforge.model.ChannelType;
import com.messageforge.model.ChannelMessage.ChannelMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelMessageRepository extends JpaRepository<ChannelMessage, UUID> {
    
    Optional<ChannelMessage> findByIdAndMessageUserId(UUID id, UUID userId);
    
    List<ChannelMessage> findByMessageId(UUID messageId);
    
    List<ChannelMessage> findByMessageIdAndChannelType(UUID messageId, ChannelType channelType);
    
    @Query("SELECT cm FROM ChannelMessage cm WHERE cm.message.user.id = :userId AND cm.channelType = :channelType AND cm.status = :status ORDER BY cm.createdAt DESC")
    Page<ChannelMessage> findByUserIdAndChannelTypeAndStatus(UUID userId, ChannelType channelType, ChannelMessageStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(cm) FROM ChannelMessage cm WHERE cm.message.user.id = :userId AND cm.channelType = :channelType AND cm.status = :status")
    long countByUserIdAndChannelTypeAndStatus(UUID userId, ChannelType channelType, ChannelMessageStatus status);
    
    @Query("SELECT cm FROM ChannelMessage cm WHERE cm.status = 'PENDING' AND cm.retryCount < 3 ORDER BY cm.createdAt ASC LIMIT :limit")
    List<ChannelMessage> findPendingMessagesForProcessing(@Param("limit") int limit);
}
