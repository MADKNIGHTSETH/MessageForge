package com.messageforge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.messageforge.decorator.*;
import com.messageforge.dto.MessageDtos.*;
import com.messageforge.exception.ApiException;
import com.messageforge.formatter.FormatterContext;
import com.messageforge.formatter.MessageFormatterStrategy;
import com.messageforge.formatter.MessageFormatterStrategy.FormattingResult;
import com.messageforge.model.*;
import com.messageforge.model.Message.MessageStatus;
import com.messageforge.model.ChannelMessage.ChannelMessageStatus;
import com.messageforge.repository.ChannelIntegrationRepository;
import com.messageforge.repository.ChannelMessageRepository;
import com.messageforge.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelMessageRepository channelMessageRepository;
    private final ChannelIntegrationRepository channelIntegrationRepository;
    private final FormatterContext formatterContext;
    private final MessageSenderService messageSenderService;

    public MessageListResponse listMessages(User user, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Message> messagePage = messageRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId(), pageable);
        
        List<MessageResponse> responses = messagePage.getContent().stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());

        return MessageListResponse.builder()
                .messages(responses)
                .page(page)
                .pageSize(pageSize)
                .totalCount(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .build();
    }

    @Transactional
    public MessageResponse createMessage(User user, CreateMessageRequest request) {
        Message message = Message.builder()
                .user(user)
                .rawContent(request.getRawContent())
                .title(request.getTitle() != null ? request.getTitle() : "Untitled Message")
                .recipient(request.getRecipient())
                .status(MessageStatus.DRAFT)
                .metadata(request.getMetadata() != null ? request.getMetadata() : JsonNodeFactory.instance.objectNode())
                .build();

        Message saved = messageRepository.save(message);
        log.info("Created message: {} for user: {}", saved.getId(), user.getEmail());
        return mapToMessageResponse(saved);
    }

    public MessageResponse getMessageDetails(User user, UUID id) {
        Message message = messageRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException("Message not found", HttpStatus.NOT_FOUND));
        return mapToMessageResponse(message);
    }

    @Transactional
    public MessageResponse updateMessage(User user, UUID id, UpdateMessageRequest request) {
        Message message = messageRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException("Message not found", HttpStatus.NOT_FOUND));

        if (message.getStatus() != MessageStatus.DRAFT && message.getStatus() != MessageStatus.FAILED) {
            throw new ApiException("Cannot update message that is sending or sent", HttpStatus.BAD_REQUEST);
        }

        message.setRawContent(request.getRawContent());
        message.setTitle(request.getTitle() != null ? request.getTitle() : message.getTitle());
        message.setRecipient(request.getRecipient() != null ? request.getRecipient() : message.getRecipient());
        if (request.getMetadata() != null) {
            message.setMetadata(request.getMetadata());
        }

        Message saved = messageRepository.save(message);
        log.info("Updated message: {}", saved.getId());
        return mapToMessageResponse(saved);
    }

    @Transactional
    public void deleteMessage(User user, UUID id) {
        Message message = messageRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException("Message not found", HttpStatus.NOT_FOUND));
        message.setDeletedAt(LocalDateTime.now());
        messageRepository.save(message);
        log.info("Soft deleted message: {}", id);
    }

    @Transactional
    public MessageResponse duplicateMessage(User user, UUID id) {
        Message message = messageRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException("Message not found", HttpStatus.NOT_FOUND));

        Message duplicate = Message.builder()
                .user(user)
                .rawContent(message.getRawContent())
                .title("Copy of " + message.getTitle())
                .status(MessageStatus.DRAFT)
                .metadata(message.getMetadata().deepCopy())
                .build();

        Message saved = messageRepository.save(duplicate);
        log.info("Duplicated message: {} into draft: {}", id, saved.getId());
        return mapToMessageResponse(saved);
    }

    public List<PreviewResponse> getPreviews(User user, UUID id) {
        Message message = messageRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException("Message not found", HttpStatus.NOT_FOUND));

        List<ChannelIntegration> integrations = channelIntegrationRepository.findEnabledIntegrationsByUserId(user.getId());
        List<PreviewResponse> previews = new ArrayList<>();

        // Extract decorators setting from message metadata
        JsonNode decorators = null;
        if (message.getMetadata() != null && message.getMetadata().has("decorators")) {
            decorators = message.getMetadata().get("decorators");
        }

        for (ChannelIntegration integration : integrations) {
            ChannelType type = integration.getChannelType();
            if (formatterContext.isChannelSupported(type)) {
                previews.add(generateSinglePreview(message.getRawContent(), type, decorators));
            }
        }

        return previews;
    }

    public List<PreviewResponse> generatePreviews(PreviewRequest request) {
        List<PreviewResponse> previews = new ArrayList<>();
        if (request.getRawContent() == null || request.getRawContent().isBlank()) {
            return previews;
        }

        List<String> activeChannels = request.getActiveChannels();
        if (activeChannels == null || activeChannels.isEmpty()) {
            return previews;
        }

        for (String chanStr : activeChannels) {
            ChannelType type = ChannelType.fromString(chanStr);
            if (formatterContext.isChannelSupported(type)) {
                previews.add(generateSinglePreview(request.getRawContent(), type, request.getDecorators()));
            }
        }

        return previews;
    }

    public PreviewResponse generateSinglePreview(String rawContent, ChannelType type, JsonNode decorators) {
        MessageFormatterStrategy baseStrategy = formatterContext.getAllStrategies().get(type);
        MessageFormatterStrategy decoratedStrategy = getDecoratedFormatter(baseStrategy, decorators);

        // Format content through decorators and strategy
        String formatted = decoratedStrategy.format(rawContent, decorators);
        FormattingResult result = decoratedStrategy.validate(formatted);

        return PreviewResponse.builder()
                .channelType(type.name())
                .formattedContent(formatted)
                .characterCount(formatted.length())
                .characterLimit(type.getMaxCharacters())
                .warning(result.warning)
                .requiresTruncation(result.requiresTruncation)
                .build();
    }

    @Transactional
    public MessageResponse sendMessage(User user, UUID id, SendMessageRequest request) {
        Message message = messageRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException("Message not found", HttpStatus.NOT_FOUND));

        if (message.getStatus() == MessageStatus.SENDING || message.getStatus() == MessageStatus.SENT) {
            throw new ApiException("Message has already been sent or is currently sending", HttpStatus.BAD_REQUEST);
        }

        List<String> targetChannels = request.getChannels();
        if (targetChannels == null || targetChannels.isEmpty()) {
            throw new ApiException("No delivery channels selected", HttpStatus.BAD_REQUEST);
        }

        // Verify integrations are configured and active
        List<ChannelIntegration> integrations = channelIntegrationRepository.findEnabledIntegrationsByUserId(user.getId());
        List<ChannelType> activeTypes = integrations.stream().map(ChannelIntegration::getChannelType).collect(Collectors.toList());

        List<ChannelType> channelsToSend = new ArrayList<>();
        for (String chanStr : targetChannels) {
            ChannelType type = ChannelType.fromString(chanStr);
            if (!activeTypes.contains(type) && !request.isTestMode()) {
                throw new ApiException("Channel integration not enabled: " + type.getDisplayName(), HttpStatus.BAD_REQUEST);
            }
            channelsToSend.add(type);
        }

        message.setStatus(MessageStatus.SENDING);
        messageRepository.save(message);

        // Extract decorator configuration
        JsonNode decorators = null;
        if (message.getMetadata() != null && message.getMetadata().has("decorators")) {
            decorators = message.getMetadata().get("decorators");
        }

        // Clear any old channel messages
        List<ChannelMessage> oldMessages = channelMessageRepository.findByMessageId(message.getId());
        channelMessageRepository.deleteAll(oldMessages);

        List<ChannelMessage> channelMessages = new ArrayList<>();
        for (ChannelType channelType : channelsToSend) {
            MessageFormatterStrategy baseStrategy = formatterContext.getAllStrategies().get(channelType);
            MessageFormatterStrategy decoratedStrategy = getDecoratedFormatter(baseStrategy, decorators);

            String formattedContent = decoratedStrategy.format(message.getRawContent(), decorators);

            ChannelMessage cm = ChannelMessage.builder()
                    .message(message)
                    .channelType(channelType)
                    .formattedContent(formattedContent)
                    .appliedDecorators(decorators != null ? decorators : JsonNodeFactory.instance.arrayNode())
                    .status(ChannelMessageStatus.PENDING)
                    .retryCount(0)
                    .build();
            
            channelMessages.add(channelMessageRepository.save(cm));
        }

        // Trigger async send dispatch
        messageSenderService.dispatchMessages(message, channelMessages, request.getRecipient(), request.isTestMode());

        MessageResponse response = mapToMessageResponse(message);
        // Include new unsent channel messages
        response.setChannelMessages(channelMessages.stream()
                .map(this::mapToChannelMessageResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private MessageFormatterStrategy getDecoratedFormatter(MessageFormatterStrategy baseFormatter, JsonNode decoratorsNode) {
        MessageFormatterStrategy strategy = baseFormatter;
        if (decoratorsNode != null) {
            if (decoratorsNode.has("urlShortener") && decoratorsNode.get("urlShortener").asBoolean()) {
                strategy = new UrlShortenerDecorator(strategy);
            }
            if (decoratorsNode.has("emoji") && decoratorsNode.get("emoji").asBoolean()) {
                strategy = new EmojiDecorator(strategy);
            }
            if (decoratorsNode.has("hashtag") && decoratorsNode.get("hashtag").asBoolean()) {
                strategy = new HashtagDecorator(strategy);
            }
            if (decoratorsNode.has("signature") && decoratorsNode.get("signature").asBoolean()) {
                strategy = new SignatureDecorator(strategy);
            }
        }
        return strategy;
    }

    private MessageResponse mapToMessageResponse(Message message) {
        List<ChannelMessage> channelMsgs = channelMessageRepository.findByMessageId(message.getId());
        List<ChannelMessageResponse> cmResponses = channelMsgs.stream()
                .map(this::mapToChannelMessageResponse)
                .collect(Collectors.toList());

        return MessageResponse.builder()
                .id(message.getId())
                .rawContent(message.getRawContent())
                .title(message.getTitle())
                .recipient(message.getRecipient())
                .status(message.getStatus().name())
                .channelMessages(cmResponses)
                .createdAt(message.getCreatedAt())
                .sentAt(message.getSentAt())
                .metadata(message.getMetadata())
                .build();
    }

    private ChannelMessageResponse mapToChannelMessageResponse(ChannelMessage cm) {
        FormattingResult result = formatterContext.validate(cm.getFormattedContent(), cm.getChannelType());
        return ChannelMessageResponse.builder()
                .id(cm.getId())
                .channelType(cm.getChannelType().name())
                .formattedContent(cm.getFormattedContent())
                .characterCount(cm.getFormattedContent().length())
                .status(cm.getStatus().name())
                .warning(result.warning)
                .requiresTruncation(result.requiresTruncation)
                .errorMessage(cm.getErrorMessage())
                .build();
    }
}
