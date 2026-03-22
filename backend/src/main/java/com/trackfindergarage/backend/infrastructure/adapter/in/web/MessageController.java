package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.MessageUseCase;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateMessageRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.MessageWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public MessageResponse createMessage(@Valid @RequestBody CreateMessageRequest request) {
        Message createdMessage = messageUseCase.createMessage(messageWebMapper.toDomain(request));
        return messageWebMapper.toResponse(createdMessage);
    }

    @GetMapping
    public List<MessageResponse> getAllMessages() {
        return messageUseCase.getAllMessages()
                .stream()
                .map(messageWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public MessageResponse getMessageById(@PathVariable Long id) {
        return messageWebMapper.toResponse(messageUseCase.getMessageById(id));
    }

    @GetMapping("/sender/{senderId}")
    public List<MessageResponse> getMessagesBySenderId(@PathVariable Long senderId) {
        return messageUseCase.getMessagesBySenderId(senderId)
                .stream()
                .map(messageWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/receiver/{receiverId}")
    public List<MessageResponse> getMessagesByReceiverId(@PathVariable Long receiverId) {
        return messageUseCase.getMessagesByReceiverId(receiverId)
                .stream()
                .map(messageWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/conversation")
    public List<MessageResponse> getConversation(@RequestParam Long userId1, @RequestParam Long userId2) {
        return messageUseCase.getConversation(userId1, userId2)
                .stream()
                .map(messageWebMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/read")
    public MessageResponse markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        return messageWebMapper.toResponse(messageUseCase.markAsRead(id, userId));
    }

    @PatchMapping("/{id}/unread")
    public MessageResponse markAsUnread(@PathVariable Long id, @RequestParam Long userId) {
        return messageWebMapper.toResponse(messageUseCase.markAsUnread(id, userId));
    }
}
