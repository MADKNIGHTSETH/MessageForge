package com.messageforge.formatter;

import com.messageforge.model.ChannelType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Strategy Pattern: Interface for channel-specific message formatting
 */
public interface MessageFormatterStrategy {
    
    /**
     * Format a raw message for a specific channel
     * @param rawContent The original message content
     * @param metadata Optional metadata (decorators, settings, etc.)
     * @return Formatted message content
     */
    String format(String rawContent, JsonNode metadata);
    
    /**
     * Get the channel type this formatter handles
     */
    ChannelType getChannelType();
    
    /**
     * Get character limit for this channel
     */
    int getCharacterLimit();
    
    /**
     * Validate if content is valid for this channel
     */
    FormattingResult validate(String content);
    
    /**
     * Result of formatting operation
     */
    class FormattingResult {
        public final boolean isValid;
        public final int characterCount;
        public final String warning;
        public final boolean requiresTruncation;
        
        public FormattingResult(boolean isValid, int characterCount) {
            this(isValid, characterCount, null, false);
        }
        
        public FormattingResult(boolean isValid, int characterCount, String warning, boolean requiresTruncation) {
            this.isValid = isValid;
            this.characterCount = characterCount;
            this.warning = warning;
            this.requiresTruncation = requiresTruncation;
        }
    }
}
