package com.messageforge.service;

import com.messageforge.dto.AdminDtos.*;
import com.messageforge.dto.AuthDtos.UserDto;
import com.messageforge.model.AuditLog;
import com.messageforge.model.ChannelMessage.ChannelMessageStatus;
import com.messageforge.model.ChannelType;
import com.messageforge.model.User;
import com.messageforge.repository.AuditLogRepository;
import com.messageforge.repository.ChannelMessageRepository;
import com.messageforge.repository.FormatterTemplateRepository;
import com.messageforge.repository.MessageRepository;
import com.messageforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChannelMessageRepository channelMessageRepository;

    @Mock
    private FormatterTemplateRepository templateRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@messageforge.local")
                .role("ADMIN")
                .build();

        targetUser = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .isActive(true)
                .build();
    }

    @Test
    void getGlobalStats_Success() {
        // Arrange
        when(userRepository.count()).thenReturn(150L);
        when(messageRepository.count()).thenReturn(5000L);

        List<Object[]> mockStats = Arrays.asList(
                new Object[]{ChannelType.EMAIL, ChannelMessageStatus.SENT, 1200L},
                new Object[]{ChannelType.EMAIL, ChannelMessageStatus.FAILED, 50L},
                new Object[]{ChannelType.SMS, ChannelMessageStatus.SENT, 800L}
        );
        when(channelMessageRepository.getChannelMessageStats()).thenReturn(mockStats);

        // Act
        GlobalStatsResponse response = adminService.getGlobalStats();

        // Assert
        assertEquals(150L, response.getTotalUsers());
        assertEquals(5000L, response.getTotalMessages());
        assertEquals(2, response.getChannelStats().size());

        ChannelStats emailStats = response.getChannelStats().stream()
                .filter(s -> s.getChannel().equals("EMAIL")).findFirst().orElseThrow();
        assertEquals(1200L, emailStats.getSent());
        assertEquals(50L, emailStats.getFailed());
        assertEquals(0L, emailStats.getPending());
    }

    @Test
    void toggleUserStatus_Success() {
        // Arrange
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(authService.mapToUserDto(any(User.class))).thenReturn(new UserDto());

        // Act
        adminService.toggleUserStatus(targetUser.getId(), false, adminUser);

        // Assert
        assertFalse(targetUser.getIsActive());
        verify(userRepository).save(targetUser);
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
