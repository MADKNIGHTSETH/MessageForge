package com.messageforge.controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.messageforge.dto.ChannelDtos.ConfigureIntegrationRequest;
import com.messageforge.dto.ChannelDtos.IntegrationResponse;
import com.messageforge.model.ChannelType;
import com.messageforge.model.User;
import com.messageforge.service.ChannelIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelIntegrationService channelIntegrationService;

    @GetMapping("/channels")
    public ResponseEntity<List<Map<String, Object>>> getChannels() {
        List<Map<String, Object>> channels = Arrays.stream(ChannelType.values())
                .map(type -> Map.<String, Object>of(
                        "name", type.name(),
                        "displayName", type.getDisplayName(),
                        "maxCharacters", type.getMaxCharacters(),
                        "exampleAddress", type.getExampleAddress(),
                        "recommendedTone", type.getRecommendedTone()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/channels/{channel}/rules")
    public ResponseEntity<ObjectNode> getRules(@PathVariable String channel) {
        ChannelType type = ChannelType.fromString(channel);
        ObjectNode rules = JsonNodeFactory.instance.objectNode();
        rules.put("channel", type.name());
        rules.put("maxCharacters", type.getMaxCharacters());
        rules.put("recommendedTone", type.getRecommendedTone());
        rules.put("requiresFormatting", type != ChannelType.EMAIL);
        rules.put("allowsEmojis", type != ChannelType.SMS);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/integrations")
    public ResponseEntity<List<IntegrationResponse>> getIntegrations(@AuthenticationPrincipal User user) {
        List<IntegrationResponse> response = channelIntegrationService.getIntegrations(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/integrations/{channel}")
    public ResponseEntity<IntegrationResponse> configureIntegration(
            @AuthenticationPrincipal User user,
            @PathVariable String channel,
            @Valid @RequestBody ConfigureIntegrationRequest request) {
        ChannelType channelType = ChannelType.fromString(channel);
        IntegrationResponse response = channelIntegrationService.configureIntegration(user, channelType, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/integrations/{channel}")
    public ResponseEntity<Void> disableIntegration(
            @AuthenticationPrincipal User user,
            @PathVariable String channel) {
        ChannelType channelType = ChannelType.fromString(channel);
        channelIntegrationService.disableIntegration(user, channelType);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/integrations/{channel}/test")
    public ResponseEntity<IntegrationResponse> testIntegration(
            @AuthenticationPrincipal User user,
            @PathVariable String channel) {
        ChannelType channelType = ChannelType.fromString(channel);
        IntegrationResponse response = channelIntegrationService.testIntegration(user, channelType);
        return ResponseEntity.ok(response);
    }
}
