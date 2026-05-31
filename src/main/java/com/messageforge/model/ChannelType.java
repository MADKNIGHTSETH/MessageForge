package com.messageforge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelType {
    EMAIL("Email", 10000, "email@example.com", "Formel / Neutre"),
    SMS("SMS", 160, "+33612345678", "Concis, direct"),
    FACEBOOK("Facebook Messenger", 20000, "user_id", "Conversationnel"),
    WHATSAPP("WhatsApp", 65536, "+33612345678", "Informel"),
    LINKEDIN("LinkedIn", 3000, "linkedin_id", "Professionnel"),
    SLACK("Slack", 4000, "@username", "Collaboratif"),
    TWITTER("Twitter/X", 280, "@username", "Percutant"),
    TELEGRAM("Telegram", 65536, "@username", "Variable");
    
    private final String displayName;
    private final int maxCharacters;
    private final String exampleAddress;
    private final String recommendedTone;
    
    public static ChannelType fromString(String value) {
        try {
            return ChannelType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown channel type: " + value);
        }
    }
}
