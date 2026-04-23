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
public class MessageController extends AbstractWebController {

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
                        authenticatedEmail(authentication),
                        request.getReceiverId(),
                        request.getSubject(),
                        request.getMessage()
                )
        );
    }

    @GetMapping
    public List<MessageResponse> getOwnMessages(Authentication authentication) {
        return mapResponses(messageUseCase.getOwnMessages(authenticatedEmail(authentication)), messageWebMapper::toResponse);
    }

    @GetMapping("/contacts")
    public List<MessageContactResponse> getAvailableRecipients(Authentication authentication) {
        return mapResponses(
                messageUseCase.getAvailableRecipients(authenticatedEmail(authentication)),
                messageWebMapper::toContactResponse
        );
    }

    @PatchMapping("/{id}/read")
    public MessageResponse markAsRead(@PathVariable Long id, Authentication authentication) {
        return messageWebMapper.toResponse(
                messageUseCase.markOwnMessageAsRead(authenticatedEmail(authentication), id)
        );
    }
}
