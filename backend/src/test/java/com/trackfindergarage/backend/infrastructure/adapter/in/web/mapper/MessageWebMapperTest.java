package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageContactResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageWebMapperTest {

    private final MessageWebMapper messageWebMapper = new MessageWebMapper();

    @Test
    void toResponseMapsMessageToResponse() {
        User sender = new User();
        sender.setId(1L);
        sender.setDisplayName("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setDisplayName("receiver");

        Message message = new Message();
        message.setId(10L);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentAt(LocalDateTime.of(2026, 3, 22, 10, 0));
        message.setIsRead(false);
        message.setSubject("Hola");
        message.setContent("Mensaje");

        MessageResponse response = messageWebMapper.toResponse(message);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getSenderId());
        assertEquals("sender", response.getSenderDisplayName());
        assertEquals(2L, response.getReceiverId());
        assertEquals("receiver", response.getReceiverDisplayName());
        assertEquals("Hola", response.getSubject());
        assertEquals("Mensaje", response.getMessage());
    }

    @Test
    void toContactResponseMapsUserToContactResponse() {
        User user = new User();
        user.setId(3L);
        user.setDisplayName("pilot");

        MessageContactResponse response = messageWebMapper.toContactResponse(user);

        assertEquals(3L, response.getId());
        assertEquals("pilot", response.getDisplayName());
    }
}
