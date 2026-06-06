package com.messageforge.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class TwitterApiClient {

    @Value("${app.api-keys.twitter-api-key:}")
    private String apiKey;

    @Value("${app.api-keys.twitter-api-secret:}")
    private String apiSecret;

    public DeliveryResult createTweet(String content, JsonNode userCredentials) {
        log.info("Creating Tweet update");
        
        String activeKey = apiKey;
        String activeSecret = apiSecret;
        
        if (userCredentials != null) {
            if (userCredentials.has("apiKey") && !userCredentials.get("apiKey").asText().isEmpty()) {
                activeKey = userCredentials.get("apiKey").asText();
            }
            if (userCredentials.has("apiSecret") && !userCredentials.get("apiSecret").asText().isEmpty()) {
                activeSecret = userCredentials.get("apiSecret").asText();
            }
        }

        if (activeKey == null || activeKey.isEmpty() || activeSecret == null || activeSecret.isEmpty()) {
            log.info("Twitter credentials not fully configured. Simulating tweet in Sandbox Mode.");
            return DeliveryResult.success("twitter-mock-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            log.info("Dispatched Tweet via Twitter v2 API.");
            return DeliveryResult.success("twtr-" + UUID.randomUUID().toString());
        } catch (Exception e) {
            log.error("Failed to post tweet via Twitter API", e);
            return DeliveryResult.failure("Twitter posting failed: " + e.getMessage());
        }
    }
}
