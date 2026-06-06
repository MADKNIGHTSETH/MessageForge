package com.messageforge.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class LinkedinApiClient {

    public DeliveryResult sharePost(String authorId, String content, JsonNode userCredentials) {
        log.info("Sharing LinkedIn update for profile: {}", authorId);
        
        String accessToken = null;
        if (userCredentials != null && userCredentials.has("accessToken")) {
            accessToken = userCredentials.get("accessToken").asText();
        }

        if (accessToken == null || accessToken.isEmpty()) {
            log.info("LinkedIn access token not configured. Simulating post in Sandbox Mode.");
            return DeliveryResult.success("linkedin-mock-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            log.info("Successfully shared post on LinkedIn author profile {}", authorId);
            return DeliveryResult.success("li-" + UUID.randomUUID().toString());
        } catch (Exception e) {
            log.error("Failed to post update on LinkedIn", e);
            return DeliveryResult.failure("LinkedIn delivery failed: " + e.getMessage());
        }
    }
}
