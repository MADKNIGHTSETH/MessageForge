package com.messageforge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messageforge.integration.*;
import com.messageforge.model.*;
import com.messageforge.model.Message.MessageStatus;
import com.messageforge.model.ChannelMessage.ChannelMessageStatus;
import com.messageforge.repository.*;
import com.messageforge.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSenderService {

    private final ChannelIntegrationRepository channelIntegrationRepository;
    private final ChannelMessageRepository channelMessageRepository;
    private final MessageRepository messageRepository;
    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // API Clients
    private final MailgunClient mailgunClient;
    private final TwilioClient twilioClient;
    private final MetaGraphApiClient metaGraphApiClient;
    private final LinkedinApiClient linkedinApiClient;
    private final SlackApiClient slackApiClient;
    private final TwitterApiClient twitterApiClient;
    private final TelegramApiClient telegramApiClient;

    @Async
    @Transactional
    public void dispatchMessages(Message message, List<ChannelMessage> channelMessages, boolean testMode) {
        log.info("Starting async message dispatching for message: {}", message.getId());
        User user = message.getUser();
        boolean overallSuccess = true;

        for (ChannelMessage cm : channelMessages) {
            cm.setStatus(ChannelMessageStatus.SENDING);
            channelMessageRepository.save(cm);
            
            // Notify WebSocket preview topic of sending status
            sendProgressUpdate(user.getId().toString(), cm.getChannelType().name(), "SENDING", null);

            JsonNode decryptedCreds = null;
            if (!testMode) {
                Optional<ChannelIntegration> integrationOpt = channelIntegrationRepository.findByUserIdAndChannelTypeNotDeleted(user.getId(), cm.getChannelType());
                if (integrationOpt.isPresent() && integrationOpt.get().getCredentials() != null) {
                    try {
                        String decryptedStr = encryptionUtil.decrypt(integrationOpt.get().getCredentials());
                        decryptedCreds = objectMapper.readTree(decryptedStr);
                    } catch (Exception e) {
                        log.error("Failed to decrypt user credentials for channel: {}", cm.getChannelType(), e);
                    }
                }
            }

            DeliveryResult result = DeliveryResult.failure("Not executed");
            try {
                String recipient = getRecipientForChannel(cm.getChannelType(), decryptedCreds);
                String content = cm.getFormattedContent();

                switch (cm.getChannelType()) {
                    case EMAIL:
                        result = mailgunClient.sendEmail(recipient, message.getTitle(), content, decryptedCreds);
                        break;
                    case SMS:
                        result = twilioClient.sendSms(recipient, content, decryptedCreds);
                        break;
                    case FACEBOOK:
                        result = metaGraphApiClient.sendFacebookMessage(recipient, content, decryptedCreds);
                        break;
                    case WHATSAPP:
                        result = metaGraphApiClient.sendWhatsappMessage(recipient, content, decryptedCreds);
                        break;
                    case LINKEDIN:
                        result = linkedinApiClient.sharePost(recipient, content, decryptedCreds);
                        break;
                    case SLACK:
                        result = slackApiClient.postMessage(recipient, content, decryptedCreds);
                        break;
                    case TWITTER:
                        result = twitterApiClient.createTweet(content, decryptedCreds);
                        break;
                    case TELEGRAM:
                        result = telegramApiClient.sendTelegramMessage(recipient, content, decryptedCreds);
                        break;
                }
            } catch (Exception e) {
                log.error("Exception occurred sending channel message: {}", cm.getId(), e);
                result = DeliveryResult.failure(e.getMessage());
            }

            // Process delivery result
            if (result.isSuccess()) {
                cm.setStatus(ChannelMessageStatus.SENT);
                cm.setSentAt(LocalDateTime.now());
                cm.setExternalMessageId(result.getExternalMessageId());
                cm.setErrorMessage(null);
                sendProgressUpdate(user.getId().toString(), cm.getChannelType().name(), "SENT", null);
            } else {
                cm.setStatus(ChannelMessageStatus.FAILED);
                cm.setErrorMessage(result.getErrorMessage());
                overallSuccess = false;
                sendProgressUpdate(user.getId().toString(), cm.getChannelType().name(), "FAILED", result.getErrorMessage());
            }
            channelMessageRepository.save(cm);
        }

        // Update parent message status
        message.setStatus(overallSuccess ? MessageStatus.SENT : MessageStatus.FAILED);
        message.setSentAt(LocalDateTime.now());
        messageRepository.save(message);
        log.info("Message sender dispatch process complete for: {}. Final status: {}", message.getId(), message.getStatus());
    }

    private String getRecipientForChannel(ChannelType type, JsonNode creds) {
        if (creds != null && creds.has("recipient")) {
            return creds.get("recipient").asText();
        }
        return type.getExampleAddress();
    }

    private void sendProgressUpdate(String userId, String channelType, String status, String errorMessage) {
        try {
            ProgressUpdate update = new ProgressUpdate(channelType, status, errorMessage);
            messagingTemplate.convertAndSend("/topic/send.progress." + userId, update);
        } catch (Exception e) {
            log.warn("Could not dispatch WebSocket progress update: {}", e.getMessage());
        }
    }

    public static class ProgressUpdate {
        public String channelType;
        public String status;
        public String errorMessage;

        public ProgressUpdate(String channelType, String status, String errorMessage) {
            this.channelType = channelType;
            this.status = status;
            this.errorMessage = errorMessage;
        }
    }
}
