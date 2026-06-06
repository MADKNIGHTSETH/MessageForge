package com.messageforge.decorator;

import com.messageforge.formatter.MessageFormatterStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.messageforge.model.ChannelType;

public class HashtagDecorator extends MessageDecorator {

    public HashtagDecorator(MessageFormatterStrategy delegate) {
        super(delegate);
    }

    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.isEmpty()) {
            return super.format(rawContent, metadata);
        }

        // Hashtags are only common on Twitter/X and LinkedIn
        if (getChannelType() != ChannelType.TWITTER && getChannelType() != ChannelType.LINKEDIN) {
            return super.format(rawContent, metadata);
        }

        StringBuilder builder = new StringBuilder(rawContent);
        
        // Check if specific hashtags are requested in metadata
        if (metadata != null && metadata.has("hashtags")) {
            JsonNode hashtagsNode = metadata.get("hashtags");
            if (hashtagsNode.isArray() && hashtagsNode.size() > 0) {
                builder.append("\n");
                for (JsonNode tag : hashtagsNode) {
                    String cleanTag = tag.asText().trim().replaceAll("^#", "");
                    if (!cleanTag.isEmpty()) {
                        builder.append(" #").append(cleanTag);
                    }
                }
            }
        } else {
            // Fallback default hashtags
            builder.append("\n#MessageForge");
        }

        return super.format(builder.toString(), metadata);
    }
}
