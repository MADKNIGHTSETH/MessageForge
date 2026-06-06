package com.messageforge.controller;

import com.messageforge.dto.MessageDtos.*;
import com.messageforge.model.User;
import com.messageforge.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<MessageListResponse> getMessages(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        MessageListResponse response = messageService.listMessages(user, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateMessageRequest request) {
        MessageResponse response = messageService.createMessage(user, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessage(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        MessageResponse response = messageService.getMessageDetails(user, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateMessage(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMessageRequest request) {
        MessageResponse response = messageService.updateMessage(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        messageService.deleteMessage(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<MessageResponse> duplicateMessage(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        MessageResponse response = messageService.duplicateMessage(user, id);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<List<PreviewResponse>> getPreviews(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        List<PreviewResponse> response = messageService.getPreviews(user, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.sendMessage(user, id, request);
        return ResponseEntity.ok(response);
    }
}
