package com.messageforge.decorator;

import com.messageforge.formatter.MessageFormatterStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.messageforge.model.ChannelType;

public class EmojiDecorator extends MessageDecorator {

    public EmojiDecorator(MessageFormatterStrategy delegate) {
        super(delegate);
    }

    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.isEmpty()) {
            return super.format(rawContent, metadata);
        }
        
        // Emojis are generally stripped or not supported in SMS
        if (getChannelType() == ChannelType.SMS) {
            return super.format(rawContent, metadata);
        }

        StringBuilder builder = new StringBuilder(rawContent);
        
        // Append context emojis
        String lower = rawContent.toLowerCase();
        if (lower.contains("welcome") || lower.contains("hello") || lower.contains("hey")) {
            builder.append(" 👋");
        }
        if (lower.contains("success") || lower.contains("done") || lower.contains("complete")) {
            builder.append(" ✅");
        }
        if (lower.contains("launch") || lower.contains("rocket") || lower.contains("start")) {
            builder.append(" 🚀");
        }
        if (lower.contains("love") || lower.contains("heart")) {
            builder.append(" ❤️");
        }

        // Channel-specific defaults
        if (getChannelType() == ChannelType.TWITTER) {
            builder.append(" ⚡");
        } else if (getChannelType() == ChannelType.SLACK) {
            builder.append(" 💬");
        } else if (getChannelType() == ChannelType.WHATSAPP || getChannelType() == ChannelType.FACEBOOK) {
            builder.append(" 😊");
        }

        return super.format(builder.toString(), metadata);
    }
}
