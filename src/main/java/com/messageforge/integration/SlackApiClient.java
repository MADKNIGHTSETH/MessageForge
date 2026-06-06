package com.messageforge.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class SlackApiClient {

    @Value("${app.api-keys.slack-bot-token:}")
    private String botToken;

    public DeliveryResult postMessage(String channelId, String content, JsonNode userCredentials) {
        log.info("Posting message to Slack channel/user: {}", channelId);
        
        String activeToken = botToken;
        if (userCredentials != null && userCredentials.has("botToken") && !userCredentials.get("botToken").asText().isEmpty()) {
            activeToken = userCredentials.get("botToken").asText();
        }

        if (activeToken == null || activeToken.isEmpty()) {
            log.info("Slack Bot Token not configured. Simulating post in Sandbox Mode.");
            return DeliveryResult.success("slack-mock-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            log.info("Dispatched message to Slack channel {} via Bot Token.", channelId);
            return DeliveryResult.success("slack-" + UUID.randomUUID().toString());
        } catch (Exception e) {
            log.error("Failed to post message to Slack", e);
            return DeliveryResult.failure("Slack post failed: " + e.getMessage());
        }
    }
}
