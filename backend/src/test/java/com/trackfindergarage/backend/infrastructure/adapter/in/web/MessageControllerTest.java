package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.MessageUseCase;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateMessageRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.MessageWebMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageControllerTest {

    private final MessageUseCase messageUseCase = mock(MessageUseCase.class);
    private final MessageWebMapper messageWebMapper = new MessageWebMapper();
    private final MessageController messageController = new MessageController(messageUseCase, messageWebMapper);

    @Test
    void createMessageDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateMessageRequest request = new CreateMessageRequest();
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setSubject("Hola");
        request.setMessage("Que tal");

        Message message = messageWithId(10L, 1L, 2L);

        when(messageUseCase.createMessage(any(Message.class))).thenReturn(message);

        MessageResponse response = messageController.createMessage(request);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getSenderId());
        assertEquals(2L, response.getReceiverId());
    }

    @Test
    void queryEndpointsMapUseCaseResult() {
        Message message = messageWithId(10L, 1L, 2L);

        when(messageUseCase.getAllMessages()).thenReturn(List.of(message));
        when(messageUseCase.getMessageById(10L)).thenReturn(message);
        when(messageUseCase.getMessagesBySenderId(1L)).thenReturn(List.of(message));
        when(messageUseCase.getMessagesByReceiverId(2L)).thenReturn(List.of(message));
        when(messageUseCase.getConversation(1L, 2L)).thenReturn(List.of(message));
        when(messageUseCase.markAsRead(10L, 2L)).thenReturn(message);
        when(messageUseCase.markAsUnread(10L, 2L)).thenReturn(message);

        assertEquals(1, messageController.getAllMessages().size());
        assertEquals(10L, messageController.getMessageById(10L).getId());
        assertEquals(1, messageController.getMessagesBySenderId(1L).size());
        assertEquals(1, messageController.getMessagesByReceiverId(2L).size());
        assertEquals(1, messageController.getConversation(1L, 2L).size());
        assertEquals(10L, messageController.markAsRead(10L, 2L).getId());
        assertEquals(10L, messageController.markAsUnread(10L, 2L).getId());
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
