package com.messageforge.formatter;

import com.messageforge.model.ChannelType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class TwitterFormatterStrategy implements MessageFormatterStrategy {

    private static final int CHARACTER_LIMIT = 280;
    private static final int TRUNCATE_LIMIT = 275;

    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }
        
        String formatted = rawContent.trim();
        if (formatted.length() > CHARACTER_LIMIT) {
            formatted = formatted.substring(0, TRUNCATE_LIMIT) + "...";
        }
        return formatted;
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.TWITTER;
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
            warning = "Twitter content is empty";
            isValid = false;
        } else if (length > CHARACTER_LIMIT) {
            warning = "Tweet exceeds 280 character limit (" + length + "/280). It will be truncated.";
            requiresTruncation = true;
        } else if (length > 260) {
            warning = "Tweet is close to character limit. " + (CHARACTER_LIMIT - length) + " characters remaining.";
        }

        return new FormattingResult(isValid, length, warning, requiresTruncation);
    }
}
