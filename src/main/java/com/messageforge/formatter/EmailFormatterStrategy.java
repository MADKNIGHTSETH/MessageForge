package com.messageforge.formatter;

import com.messageforge.model.ChannelType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

@Component
@Slf4j
public class EmailFormatterStrategy implements MessageFormatterStrategy {
    
    private final PolicyFactory htmlPolicy;
    
    public EmailFormatterStrategy() {
        // Configure safe HTML policy for emails
        this.htmlPolicy = new HtmlPolicyBuilder()
                .allowElements("p", "div", "span", "strong", "em", "u", "a", "br", "ul", "ol", "li")
                .allowAttributes("href").onElements("a")
                .requireRelNofollowOnLinks()
                .toFactory();
    }
    
    @Override
    public String format(String rawContent, JsonNode metadata) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }
        
        // Convert line breaks to HTML paragraphs
        String htmlContent = rawContent.replace("\n\n", "</p><p>").replace("\n", "<br/>");
        htmlContent = "<p>" + htmlContent + "</p>";
        
        // Apply HTML sanitization
        String sanitized = htmlPolicy.sanitize(htmlContent);
        
        // Build email HTML structure
        StringBuilder emailHtml = new StringBuilder();
        emailHtml.append("<!DOCTYPE html>\n");
        emailHtml.append("<html>\n");
        emailHtml.append("<head>\n");
        emailHtml.append("    <meta charset=\"UTF-8\">\n");
        emailHtml.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        emailHtml.append("    <style>\n");
        emailHtml.append("        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n");
        emailHtml.append("        p { margin: 10px 0; }\n");
        emailHtml.append("        a { color: #0066cc; text-decoration: none; }\n");
        emailHtml.append("    </style>\n");
        emailHtml.append("</head>\n");
        emailHtml.append("<body>\n");
        emailHtml.append(sanitized);
        emailHtml.append("\n</body>\n");
        emailHtml.append("</html>\n");
        
        return emailHtml.toString();
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }
    
    @Override
    public int getCharacterLimit() {
        return 10000;
    }
    
    @Override
    public FormattingResult validate(String content) {
        if (content == null) {
            return new FormattingResult(false, 0, "Content cannot be null", false);
        }
        
        int length = content.length();
        boolean isValid = length > 0 && length <= getCharacterLimit();
        String warning = null;
        boolean requiresTruncation = false;
        
        if (length == 0) {
            warning = "Email content is empty";
            isValid = false;
        } else if (length > 5000) {
            warning = "Email is quite long (" + length + " characters). Consider keeping it under 5000 for better deliverability.";
        } else if (length > getCharacterLimit()) {
            warning = "Email exceeds maximum character limit (" + length + "/" + getCharacterLimit() + ")";
            requiresTruncation = true;
        }
        
        return new FormattingResult(isValid, length, warning, requiresTruncation);
    }
}
