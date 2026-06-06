package com.messageforge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messageforge.dto.ChannelDtos.ConfigureIntegrationRequest;
import com.messageforge.dto.ChannelDtos.IntegrationResponse;
import com.messageforge.exception.ApiException;
import com.messageforge.integration.*;
import com.messageforge.model.ChannelIntegration;
import com.messageforge.model.ChannelIntegration.TestStatus;
import com.messageforge.model.ChannelType;
import com.messageforge.model.User;
import com.messageforge.repository.ChannelIntegrationRepository;
import com.messageforge.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelIntegrationService {

    private final ChannelIntegrationRepository channelIntegrationRepository;
    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;

    // API Clients
    private final MailgunClient mailgunClient;
    private final TwilioClient twilioClient;
    private final MetaGraphApiClient metaGraphApiClient;
    private final LinkedinApiClient linkedinApiClient;
    private final SlackApiClient slackApiClient;
    private final TwitterApiClient twitterApiClient;
    private final TelegramApiClient telegramApiClient;

    public List<IntegrationResponse> getIntegrations(User user) {
        List<ChannelIntegration> integrations = channelIntegrationRepository.findByUserIdAndDeletedAtIsNull(user.getId());
        return integrations.stream()
                .map(this::mapToIntegrationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IntegrationResponse configureIntegration(User user, ChannelType channelType, ConfigureIntegrationRequest request) {
        Optional<ChannelIntegration> existingOpt = channelIntegrationRepository.findByUserIdAndChannelTypeNotDeleted(user.getId(), channelType);
        
        ChannelIntegration integration;
        if (existingOpt.isPresent()) {
            integration = existingOpt.get();
        } else {
            integration = ChannelIntegration.builder()
                    .user(user)
                    .channelType(channelType)
                    .build();
        }

        integration.setIsEnabled(request.isEnabled());
        if (request.getSettings() != null) {
            integration.setSettings(request.getSettings());
        }

        // Encrypt credentials JSON
        if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
            try {
                String credStr = objectMapper.writeValueAsString(request.getCredentials());
                byte[] encrypted = encryptionUtil.encrypt(credStr);
                integration.setCredentials(encrypted);
            } catch (Exception e) {
                throw new ApiException("Failed to encrypt credentials: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        ChannelIntegration saved = channelIntegrationRepository.save(integration);
        log.info("Configured channel integration {} for user: {}", channelType, user.getEmail());
        return mapToIntegrationResponse(saved);
    }

    @Transactional
    public void disableIntegration(User user, ChannelType channelType) {
        ChannelIntegration integration = channelIntegrationRepository.findByUserIdAndChannelTypeNotDeleted(user.getId(), channelType)
                .orElseThrow(() -> new ApiException("Channel integration not found", HttpStatus.NOT_FOUND));

        integration.setIsEnabled(false);
        channelIntegrationRepository.save(integration);
        log.info("Disabled channel integration {} for user: {}", channelType, user.getEmail());
    }

    @Transactional
    public IntegrationResponse testIntegration(User user, ChannelType channelType) {
        ChannelIntegration integration = channelIntegrationRepository.findByUserIdAndChannelTypeNotDeleted(user.getId(), channelType)
                .orElseThrow(() -> new ApiException("Channel integration not found", HttpStatus.NOT_FOUND));

        JsonNode decryptedCreds = null;
        if (integration.getCredentials() != null) {
            try {
                String decryptedStr = encryptionUtil.decrypt(integration.getCredentials());
                decryptedCreds = objectMapper.readTree(decryptedStr);
            } catch (Exception e) {
                log.error("Failed to decrypt credentials during test", e);
                throw new ApiException("Credential integrity error during decryption", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        DeliveryResult result;
        String testContent = "This is a test notification from MessageForge to verify API connectivity. ✅";
        String testRecipient = getTestRecipient(channelType, decryptedCreds);

        switch (channelType) {
            case EMAIL:
                result = mailgunClient.sendEmail(testRecipient, "MessageForge Integration Test", testContent, decryptedCreds);
                break;
            case SMS:
                result = twilioClient.sendSms(testRecipient, testContent, decryptedCreds);
                break;
            case FACEBOOK:
                result = metaGraphApiClient.sendFacebookMessage(testRecipient, testContent, decryptedCreds);
                break;
            case WHATSAPP:
                result = metaGraphApiClient.sendWhatsappMessage(testRecipient, testContent, decryptedCreds);
                break;
            case LINKEDIN:
                result = linkedinApiClient.sharePost(testRecipient, testContent, decryptedCreds);
                break;
            case SLACK:
                result = slackApiClient.postMessage(testRecipient, testContent, decryptedCreds);
                break;
            case TWITTER:
                result = twitterApiClient.createTweet(testContent, decryptedCreds);
                break;
            case TELEGRAM:
                result = telegramApiClient.sendTelegramMessage(testRecipient, testContent, decryptedCreds);
                break;
            default:
                throw new ApiException("Unsupported test channel: " + channelType, HttpStatus.BAD_REQUEST);
        }

        integration.setLastTestedAt(LocalDateTime.now());
        integration.setTestStatus(result.isSuccess() ? TestStatus.SUCCESS : TestStatus.FAILED);
        ChannelIntegration saved = channelIntegrationRepository.save(integration);
        
        log.info("Tested channel integration {} for user: {}. Result: {}", channelType, user.getEmail(), integration.getTestStatus());
        return mapToIntegrationResponse(saved);
    }

    private String getTestRecipient(ChannelType type, JsonNode creds) {
        if (creds != null && creds.has("testRecipient")) {
            return creds.get("testRecipient").asText();
        }
        return type.getExampleAddress();
    }

    private IntegrationResponse mapToIntegrationResponse(ChannelIntegration ci) {
        return IntegrationResponse.builder()
                .channelType(ci.getChannelType().name())
                .displayName(ci.getChannelType().getDisplayName())
                .enabled(ci.getIsEnabled())
                .settings(ci.getSettings())
                .lastTestedAt(ci.getLastTestedAt())
                .testStatus(ci.getTestStatus() != null ? ci.getTestStatus().name() : "UNTESTED")
                .build();
    }
}
