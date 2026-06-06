package com.messageforge.controller;

import com.messageforge.dto.MessageDtos.PreviewRequest;
import com.messageforge.dto.MessageDtos.PreviewResponse;
import com.messageforge.model.ChannelType;
import com.messageforge.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/preview.request")
    public void handlePreviewRequest(PreviewRequest request, Principal principal) {
        log.info("Processing WebSocket preview request");
        
        // Use principal name (email) as the identifier, default to a fallback for simple testing
        String userId = principal != null ? principal.getName() : "default";

        List<String> activeChannels = request.getActiveChannels();
        if (activeChannels == null || activeChannels.isEmpty()) {
            return;
        }

        for (String chanStr : activeChannels) {
            try {
                ChannelType type = ChannelType.fromString(chanStr);
                PreviewResponse preview = messageService.generateSinglePreview(
                        request.getRawContent(),
                        type,
                        request.getDecorators()
                );
                
                // Broadcast to user-specific preview topic
                messagingTemplate.convertAndSend("/topic/preview." + userId, preview);
            } catch (Exception e) {
                log.error("Error generating live WebSocket preview for channel: {}", chanStr, e);
            }
        }
    }
}
