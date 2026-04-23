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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageControllerTest {

    private final MessageUseCase messageUseCase = mock(MessageUseCase.class);
    private final MessageWebMapper messageWebMapper = new MessageWebMapper();
    private final MessageController messageController = new MessageController(messageUseCase, messageWebMapper);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void createMessageDelegatesToUseCaseAndMapsResponse() {
        CreateOwnMessageRequest request = new CreateOwnMessageRequest();
        request.setReceiverId(2L);
        request.setSubject("Hola");
        request.setMessage("Mensaje");

        when(authentication.getName()).thenReturn("driver@example.com");
        when(messageUseCase.createOwnMessage("driver@example.com", 2L, "Hola", "Mensaje"))
                .thenReturn(message(10L, 1L, "driver", 2L, "organizer"));

        MessageResponse response = messageController.createMessage(authentication, request);

        assertEquals(10L, response.getId());
        assertEquals("Hola", response.getSubject());
        assertEquals("Mensaje", response.getMessage());
        verify(messageUseCase).createOwnMessage("driver@example.com", 2L, "Hola", "Mensaje");
    }

    @Test
    void getOwnMessagesMapsResponseList() {
        when(authentication.getName()).thenReturn("driver@example.com");
        when(messageUseCase.getOwnMessages("driver@example.com"))
                .thenReturn(List.of(message(10L, 1L, "driver", 2L, "organizer")));

        List<MessageResponse> responses = messageController.getOwnMessages(authentication);

        assertEquals(1, responses.size());
        assertEquals("driver", responses.get(0).getSenderDisplayName());
    }

    @Test
    void getAvailableRecipientsMapsContacts() {
        when(authentication.getName()).thenReturn("driver@example.com");
        when(messageUseCase.getAvailableRecipients("driver@example.com"))
                .thenReturn(List.of(user(2L, "organizer"), user(3L, "marshall")));

        List<MessageContactResponse> responses = messageController.getAvailableRecipients(authentication);

        assertEquals(2, responses.size());
        assertEquals("organizer", responses.get(0).getDisplayName());
    }

    @Test
    void markAsReadDelegatesToUseCaseAndMapsResponse() {
        when(authentication.getName()).thenReturn("driver@example.com");
        when(messageUseCase.markOwnMessageAsRead("driver@example.com", 10L))
                .thenReturn(message(10L, 1L, "driver", 2L, "organizer"));

        MessageResponse response = messageController.markAsRead(10L, authentication);

        assertEquals(10L, response.getId());
        verify(messageUseCase).markOwnMessageAsRead("driver@example.com", 10L);
    }

    private Message message(Long id,
                            Long senderId,
                            String senderDisplayName,
                            Long receiverId,
                            String receiverDisplayName) {
        User sender = user(senderId, senderDisplayName);
        User receiver = user(receiverId, receiverDisplayName);

        Message message = new Message();
        message.setId(id);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject("Hola");
        message.setContent("Mensaje");
        message.setIsRead(true);
        message.setSentAt(LocalDateTime.now());
        return message;
    }

    private User user(Long id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        return user;
    }
}
