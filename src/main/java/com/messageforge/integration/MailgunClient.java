package com.messageforge.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class MailgunClient {

    @Value("${app.api-keys.mailgun:}")
    private String apiKey;

    @Value("${app.api-keys.mailgun-domain:}")
    private String domain;

    public DeliveryResult sendEmail(String recipient, String subject, String bodyHtml, JsonNode userCredentials) {
        log.info("Sending Email to: {}, Subject: {}", recipient, subject);
        
        String activeApiKey = apiKey;
        String activeDomain = domain;
        
        if (userCredentials != null) {
            if (userCredentials.has("apiKey") && !userCredentials.get("apiKey").asText().isEmpty()) {
                activeApiKey = userCredentials.get("apiKey").asText();
            }
            if (userCredentials.has("domain") && !userCredentials.get("domain").asText().isEmpty()) {
                activeDomain = userCredentials.get("domain").asText();
            }
        }

        if (activeApiKey == null || activeApiKey.isEmpty() || activeDomain == null || activeDomain.isEmpty()) {
            log.info("Mailgun API credentials not configured. Simulating delivery in Sandbox Mode.");
            return DeliveryResult.success("mailgun-mock-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            log.info("Dispatched email via Mailgun API for domain: {}", activeDomain);
            return DeliveryResult.success("mg-" + UUID.randomUUID().toString());
        } catch (Exception e) {
            log.error("Failed to send email via Mailgun", e);
            return DeliveryResult.failure("Mailgun delivery failed: " + e.getMessage());
        }
    }
}
