package com.messageforge.formatter;

import com.messageforge.model.ChannelType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy Pattern Context: Manages and executes formatting strategies for different channels
 * Implements Open/Closed Principle - easily extensible with new channels
 */
@Component
@Slf4j
public class FormatterContext {
    
    private final Map<ChannelType, MessageFormatterStrategy> strategies = new HashMap<>();
    
    public FormatterContext(List<MessageFormatterStrategy> formatterStrategies) {
        // Register all strategies
        formatterStrategies.forEach(strategy -> {
            strategies.put(strategy.getChannelType(), strategy);
            log.debug("Registered formatter strategy for channel: {}", strategy.getChannelType());
        });
    }
    
    /**
     * Format a message using the strategy for a specific channel
     */
    public String format(String rawContent, ChannelType channelType, JsonNode metadata) {
        MessageFormatterStrategy strategy = getStrategy(channelType);
        return strategy.format(rawContent, metadata);
    }
    
    /**
     * Validate message for a specific channel
     */
    public MessageFormatterStrategy.FormattingResult validate(String content, ChannelType channelType) {
        MessageFormatterStrategy strategy = getStrategy(channelType);
        return strategy.validate(content);
    }
    
    /**
     * Get character limit for a channel
     */
    public int getCharacterLimit(ChannelType channelType) {
        return getStrategy(channelType).getCharacterLimit();
    }
    
    /**
     * Get all registered strategies (for UI to show all available channels)
     */
    public Map<ChannelType, MessageFormatterStrategy> getAllStrategies() {
        return Map.copyOf(strategies);
    }
    
    /**
     * Check if a channel formatter is registered
     */
    public boolean isChannelSupported(ChannelType channelType) {
        return strategies.containsKey(channelType);
    }
    
    /**
     * Get strategy for a channel
     * @throws IllegalArgumentException if channel is not supported
     */
    private MessageFormatterStrategy getStrategy(ChannelType channelType) {
        MessageFormatterStrategy strategy = strategies.get(channelType);
        if (strategy == null) {
            throw new IllegalArgumentException("Formatter not found for channel: " + channelType);
        }
        return strategy;
    }
}
