package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.MessageUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOwnMessageRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageContactResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.MessageWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageUseCase messageUseCase;
    private final MessageWebMapper messageWebMapper;

    public MessageController(MessageUseCase messageUseCase, MessageWebMapper messageWebMapper) {
        this.messageUseCase = messageUseCase;
        this.messageWebMapper = messageWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createMessage(Authentication authentication,
                                         @Valid @RequestBody CreateOwnMessageRequest request) {
        return messageWebMapper.toResponse(
                messageUseCase.createOwnMessage(
                        authentication != null ? authentication.getName() : null,
                        request.getReceiverId(),
                        request.getSubject(),
                        request.getMessage()
                )
        );
    }

    @GetMapping
    public List<MessageResponse> getOwnMessages(Authentication authentication) {
        return messageUseCase.getOwnMessages(authentication != null ? authentication.getName() : null)
                .stream()
                .map(messageWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/contacts")
    public List<MessageContactResponse> getAvailableRecipients(Authentication authentication) {
        return messageUseCase.getAvailableRecipients(authentication != null ? authentication.getName() : null)
                .stream()
                .map(messageWebMapper::toContactResponse)
                .toList();
    }

    @GetMapping("/conversation/{counterpartId}")
    public List<MessageResponse> getOwnConversation(@PathVariable Long counterpartId,
                                                    Authentication authentication) {
        return messageUseCase.getOwnConversation(
                        authentication != null ? authentication.getName() : null,
                        counterpartId
                )
                .stream()
                .map(messageWebMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/read")
    public MessageResponse markAsRead(@PathVariable Long id, Authentication authentication) {
        return messageWebMapper.toResponse(
                messageUseCase.markOwnMessageAsRead(authentication != null ? authentication.getName() : null, id)
        );
    }

    @PatchMapping("/{id}/unread")
    public MessageResponse markAsUnread(@PathVariable Long id, Authentication authentication) {
        return messageWebMapper.toResponse(
                messageUseCase.markOwnMessageAsUnread(authentication != null ? authentication.getName() : null, id)
        );
    }
}
