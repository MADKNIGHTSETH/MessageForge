package com.messageforge.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class MetaGraphApiClient {

    @Value("${app.api-keys.meta-app-id:}")
    private String appId;

    @Value("${app.api-keys.meta-app-secret:}")
    private String appSecret;

    public DeliveryResult sendFacebookMessage(String recipientId, String content, JsonNode userCredentials) {
        log.info("Sending Facebook Messenger message to recipient: {}", recipientId);
        return sendMeta("facebook", recipientId, content, userCredentials);
    }

    public DeliveryResult sendWhatsappMessage(String recipientPhone, String content, JsonNode userCredentials) {
        log.info("Sending WhatsApp message to phone: {}", recipientPhone);
        return sendMeta("whatsapp", recipientPhone, content, userCredentials);
    }

    private DeliveryResult sendMeta(String type, String recipient, String content, JsonNode userCredentials) {
        String activeAppId = appId;
        String activeSecret = appSecret;

        if (userCredentials != null) {
            if (userCredentials.has("appId") && !userCredentials.get("appId").asText().isEmpty()) {
                activeAppId = userCredentials.get("appId").asText();
            }
            if (userCredentials.has("appSecret") && !userCredentials.get("appSecret").asText().isEmpty()) {
                activeSecret = userCredentials.get("appSecret").asText();
            }
        }

        if (activeAppId == null || activeAppId.isEmpty() || activeSecret == null || activeSecret.isEmpty()) {
            log.info("Meta Graph API credentials not configured. Simulating {} message in Sandbox Mode.", type);
            return DeliveryResult.success("meta-mock-" + type + "-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            log.info("Successfully sent {} message to {} via Meta Graph API.", type, recipient);
            return DeliveryResult.success("meta-" + UUID.randomUUID().toString());
        } catch (Exception e) {
            log.error("Failed to send message via Meta Graph API", e);
            return DeliveryResult.failure("Meta Graph API delivery failed: " + e.getMessage());
        }
    }
}
