package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.MessageUseCase;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOwnMessageRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageContactResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.MessageWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageControllerTest {

    private final MessageUseCase messageUseCase = mock(MessageUseCase.class);
    private final MessageWebMapper messageWebMapper = new MessageWebMapper();
    private final MessageController messageController = new MessageController(messageUseCase, messageWebMapper);

    @Test
    void createMessageDelegatesToUseCaseAndReturnsMappedResponse() {
        Authentication authentication = authentication("sender@example.com");
        CreateOwnMessageRequest request = new CreateOwnMessageRequest();
        request.setReceiverId(2L);
        request.setSubject("Hola");
        request.setMessage("Que tal");

        Message message = messageWithId(10L, 1L, 2L);

        when(messageUseCase.createOwnMessage("sender@example.com", 2L, "Hola", "Que tal")).thenReturn(message);

        MessageResponse response = messageController.createMessage(authentication, request);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getSenderId());
        assertEquals(2L, response.getReceiverId());
    }

    @Test
    void queryEndpointsMapUseCaseResult() {
        Authentication authentication = authentication("sender@example.com");
        Message message = messageWithId(10L, 1L, 2L);
        User receiver = new User();
        receiver.setId(2L);
        receiver.setDisplayName("receiver");

        when(messageUseCase.getOwnMessages("sender@example.com")).thenReturn(List.of(message));
        when(messageUseCase.getOwnConversation("sender@example.com", 2L)).thenReturn(List.of(message));
        when(messageUseCase.getAvailableRecipients("sender@example.com")).thenReturn(List.of(receiver));
        when(messageUseCase.markOwnMessageAsRead("sender@example.com", 10L)).thenReturn(message);
        when(messageUseCase.markOwnMessageAsUnread("sender@example.com", 10L)).thenReturn(message);

        assertEquals(1, messageController.getOwnMessages(authentication).size());
        assertEquals(1, messageController.getOwnConversation(2L, authentication).size());
        List<MessageContactResponse> contacts = messageController.getAvailableRecipients(authentication);
        assertEquals(1, contacts.size());
        assertEquals(2L, contacts.get(0).getId());
        assertEquals(10L, messageController.markAsRead(10L, authentication).getId());
        assertEquals(10L, messageController.markAsUnread(10L, authentication).getId());
    }

    private Authentication authentication(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        return authentication;
    }

    private Message messageWithId(Long id, Long senderId, Long receiverId) {
        User sender = new User();
        sender.setId(senderId);
        sender.setDisplayName("sender");

        User receiver = new User();
        receiver.setId(receiverId);
        receiver.setDisplayName("receiver");

        Message message = new Message();
        message.setId(id);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentAt(LocalDateTime.of(2026, 3, 22, 10, 0));
        message.setIsRead(false);
        message.setSubject("Hola");
        message.setContent("Que tal");
        return message;
    }
}
