package com.messageforge.repository;

import com.messageforge.model.Message;
import com.messageforge.model.Message.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    
    Optional<Message> findByIdAndUserId(UUID id, UUID userId);
    
    Page<Message> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND m.deletedAt IS NULL AND m.status = :status ORDER BY m.createdAt DESC")
    Page<Message> findByUserIdAndStatus(UUID userId, MessageStatus status, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND m.deletedAt IS NULL AND m.status IN :statuses ORDER BY m.createdAt DESC")
    Page<Message> findByUserIdAndStatuses(UUID userId, List<MessageStatus> statuses, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.user.id = :userId AND m.deletedAt IS NULL")
    long countByUserId(UUID userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.user.id = :userId AND m.status = :status AND m.deletedAt IS NULL")
    long countByUserIdAndStatus(UUID userId, MessageStatus status);
    
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND m.createdAt BETWEEN :startDate AND :endDate AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    List<Message> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
}
