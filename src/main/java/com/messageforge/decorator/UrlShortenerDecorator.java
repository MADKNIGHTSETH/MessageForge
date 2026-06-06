package com.messageforge.decorator;

import com.messageforge.formatter.MessageFormatterStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

public class UrlShortenerDecorator extends MessageDecorator {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+|www\\.\\S+");

    public UrlShortenerDecorator(MessageFormatterStrategy delegate) {
        super(delegate);
    }

    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.isEmpty()) {
            return super.format(rawContent, metadata);
        }

        // Find and shorten long URLs in raw text
        Matcher matcher = URL_PATTERN.matcher(rawContent);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group();
            if (url.length() > 20) {
                String shortCode = UUID.randomUUID().toString().substring(0, 6);
                matcher.appendReplacement(sb, "https://forge.lnk/" + shortCode);
            } else {
                matcher.appendReplacement(sb, url);
            }
        }
        matcher.appendTail(sb);

        return super.format(sb.toString(), metadata);
    }
}
