package com.messageforge.formatter;

import com.messageforge.model.ChannelType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class FacebookFormatterStrategy implements MessageFormatterStrategy {

    private static final int CHARACTER_LIMIT = 20000;

    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }
        // Facebook supports emojis and paragraphs. Keep formatting readable.
        return rawContent.trim();
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.FACEBOOK;
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
            warning = "Facebook Messenger content is empty";
            isValid = false;
        } else if (length > CHARACTER_LIMIT) {
            warning = "Content exceeds Facebook Messenger character limit of " + CHARACTER_LIMIT;
            requiresTruncation = true;
        }

        return new FormattingResult(isValid, length, warning, requiresTruncation);
    }
}
