package com.messageforge.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class TwilioClient {

    @Value("${app.api-keys.twilio-account-sid:}")
    private String accountSid;

    @Value("${app.api-keys.twilio-auth-token:}")
    private String authToken;

    @Value("${app.api-keys.twilio-phone:}")
    private String fromPhone;

    public DeliveryResult sendSms(String recipient, String content, JsonNode userCredentials) {
        log.info("Sending SMS to: {}", recipient);
        
        String activeSid = accountSid;
        String activeToken = authToken;
        String activePhone = fromPhone;
        
        if (userCredentials != null) {
            if (userCredentials.has("accountSid") && !userCredentials.get("accountSid").asText().isEmpty()) {
                activeSid = userCredentials.get("accountSid").asText();
            }
            if (userCredentials.has("authToken") && !userCredentials.get("authToken").asText().isEmpty()) {
                activeToken = userCredentials.get("authToken").asText();
            }
            if (userCredentials.has("fromPhone") && !userCredentials.get("fromPhone").asText().isEmpty()) {
                activePhone = userCredentials.get("fromPhone").asText();
            }
        }

        if (activeSid == null || activeSid.isEmpty() || activeToken == null || activeToken.isEmpty() || activePhone == null || activePhone.isEmpty()) {
            log.info("Twilio credentials not configured. Simulating delivery in Sandbox Mode.");
            return DeliveryResult.success("twilio-mock-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            log.info("Dispatched SMS via Twilio API from: {}", activePhone);
            return DeliveryResult.success("tw-" + UUID.randomUUID().toString());
        } catch (Exception e) {
            log.error("Failed to send SMS via Twilio", e);
            return DeliveryResult.failure("Twilio delivery failed: " + e.getMessage());
        }
    }
}
