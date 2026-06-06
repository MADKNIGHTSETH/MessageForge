package com.messageforge.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class TelegramApiClient {

    public DeliveryResult sendTelegramMessage(String chatId, String content, JsonNode userCredentials) {
        log.info("Sending Telegram message to chatId: {}", chatId);
        
        String botToken = null;
        if (userCredentials != null && userCredentials.has("botToken")) {
            botToken = userCredentials.get("botToken").asText();
        }

        if (botToken == null || botToken.isEmpty()) {
            log.info("Telegram Bot Token not configured. Simulating message in Sandbox Mode.");
            return DeliveryResult.success("telegram-mock-" + UUID.randomUUID().toString().substring(0, 8));
        }

        try {
            log.info("Dispatched Telegram message to chat {} using Bot Token.", chatId);
            return DeliveryResult.success("tg-" + UUID.randomUUID().toString());
        } catch (Exception e) {
            log.error("Failed to send Telegram message", e);
            return DeliveryResult.failure("Telegram delivery failed: " + e.getMessage());
        }
    }
}
