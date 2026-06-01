package com.messageforge.formatter;

import com.messageforge.model.ChannelType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class SmsFormatterStrategy implements MessageFormatterStrategy {
    
    private static final int CHARACTER_LIMIT = 160;
    private static final int TRUNCATE_LIMIT = 155;
    private static final Pattern EMOJI_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\s\\p{P}]", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+|www\\.\\S+");
    
    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }
        
        // Remove multiple line breaks
        String formatted = rawContent.replaceAll("\\n\\s*\\n", " ").replaceAll("\\n", " ");
        
        // Check if decorators include URL shortener
        boolean useUrlShortener = false;
        if (metadata != null && metadata.has("decorators")) {
            JsonNode decorators = metadata.get("decorators");
            if (decorators.has("urlShortener")) {
                useUrlShortener = decorators.get("urlShortener").asBoolean();
            }
        }
        
        // Handle URLs
        if (useUrlShortener) {
            formatted = shortenUrls(formatted);
        } else {
            // Remove URLs if not using shortener
            formatted = URL_PATTERN.matcher(formatted).replaceAll("[URL]");
        }
        
        // Remove emojis
        formatted = removeEmojis(formatted);
        
        // Truncate to character limit
        if (formatted.length() > TRUNCATE_LIMIT) {
            formatted = formatted.substring(0, TRUNCATE_LIMIT) + "...";
        }
        
        return formatted.trim();
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }
    
    @Override
    public int getCharacterLimit() {
        return CHARACTER_LIMIT;
    }
    
    @Override
    public FormattingResult validate(String content) {
        if (content == null) {
            return new FormattingResult(false, 0, "Content cannot be null", false);
        }
        
        int length = content.length();
        boolean isValid = length > 0 && length <= CHARACTER_LIMIT;
        String warning = null;
        boolean requiresTruncation = false;
        
        if (length == 0) {
            warning = "SMS content is empty";
            isValid = false;
        } else if (length > CHARACTER_LIMIT) {
            warning = "SMS exceeds GSM 7-bit limit (" + length + "/" + CHARACTER_LIMIT + "). Will be split into multiple parts.";
            requiresTruncation = true;
        } else if (length > 145) {
            warning = "SMS is close to character limit. " + (CHARACTER_LIMIT - length) + " characters remaining.";
        }
        
        return new FormattingResult(isValid, length, warning, requiresTruncation);
    }
    
    private String shortenUrls(String content) {
        // TODO: Replace this placeholder with a real URL shortening provider integration.
        return URL_PATTERN.matcher(content).replaceAll("[bit.ly/...]");
    }
    
    private String removeEmojis(String content) {
        return EMOJI_PATTERN.matcher(content).replaceAll("");
    }
}
