package com.messageforge.decorator;

import com.messageforge.formatter.MessageFormatterStrategy;
import com.messageforge.model.ChannelType;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class MessageDecorator implements MessageFormatterStrategy {
    
    protected final MessageFormatterStrategy delegate;

    protected MessageDecorator(MessageFormatterStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public String format(String rawContent, JsonNode metadata) {
        return delegate.format(rawContent, metadata);
    }

    @Override
    public ChannelType getChannelType() {
        return delegate.getChannelType();
    }

    @Override
    public int getCharacterLimit() {
        return delegate.getCharacterLimit();
    }

    @Override
    public FormattingResult validate(String content) {
        return delegate.validate(content);
    }
}
