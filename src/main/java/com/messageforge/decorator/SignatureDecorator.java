package com.messageforge.decorator;

import com.messageforge.formatter.MessageFormatterStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.messageforge.model.ChannelType;

public class SignatureDecorator extends MessageDecorator {

    public SignatureDecorator(MessageFormatterStrategy delegate) {
        super(delegate);
    }

    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return super.format(rawContent, metadata);
        }

        // SMS/Twitter are character-constrained, so we usually don't append a signature
        if (getChannelType() == ChannelType.SMS || getChannelType() == ChannelType.TWITTER) {
            return super.format(rawContent, metadata);
        }

        StringBuilder builder = new StringBuilder(rawContent);

        // Fetch signature from metadata or use default
        String signature = null;
        if (metadata != null && metadata.has("signature")) {
            signature = metadata.get("signature").asText();
        }

        if (signature != null && !signature.trim().isEmpty()) {
            builder.append("\n\n").append(signature);
        } else {
            builder.append("\n\nWarm regards,\nSent via MessageForge");
        }

        return super.format(builder.toString(), metadata);
    }
}
