package com.messageforge.formatter;

import com.messageforge.model.ChannelType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class SlackFormatterStrategy implements MessageFormatterStrategy {

    private static final int CHARACTER_LIMIT = 4000;

    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }
        // Slack uses markdown style (*bold*, _italic_, `code`, etc.)
        return rawContent.trim();
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SLACK;
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
            warning = "Slack content is empty";
            isValid = false;
        } else if (length > CHARACTER_LIMIT) {
            warning = "Content exceeds Slack character limit of " + CHARACTER_LIMIT;
            requiresTruncation = true;
        }

        return new FormattingResult(isValid, length, warning, requiresTruncation);
    }
}
