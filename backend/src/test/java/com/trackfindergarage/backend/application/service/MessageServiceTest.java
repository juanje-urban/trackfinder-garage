package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.MessagePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessagePersistencePort messagePersistencePort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @InjectMocks
    private MessageService messageService;

    @Test
    void getOwnMessagesReturnsMessagesForAuthenticatedUser() {
        User currentUser = user(1L, "driver", true);
        Message message = message(10L, currentUser, user(2L, "organizer", true));
        List<Message> messages = List.of(message);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(currentUser));
        when(messagePersistencePort.findByParticipantIdOrderBySentAtAsc(1L)).thenReturn(messages);

        assertEquals(messages, messageService.getOwnMessages(" DRIVER@example.com "));
    }

    @Test
    void createOwnMessageStoresMessageWithDefaults() {
        User sender = user(1L, "driver", true);
        User receiver = user(2L, "organizer", true);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(sender));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(receiver));
        when(messagePersistencePort.save(any(Message.class))).then(returnsFirstArg());

        Message createdMessage = messageService.createOwnMessage("driver@example.com", 2L, "Hola", "Mensaje");

        assertSame(sender, createdMessage.getSender());
        assertSame(receiver, createdMessage.getReceiver());
        assertEquals("Hola", createdMessage.getSubject());
        assertEquals("Mensaje", createdMessage.getContent());
        assertEquals(Boolean.FALSE, createdMessage.getIsRead());
        assertNotNull(createdMessage.getSentAt());
        verify(messagePersistencePort).save(createdMessage);
    }

    @Test
    void createOwnMessageThrowsWhenReceiverIsDisabled() {
        User sender = user(1L, "driver", true);
        User disabledReceiver = user(2L, "organizer", false);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(sender));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(disabledReceiver));

        assertThrows(
                IllegalArgumentException.class,
                () -> messageService.createOwnMessage("driver@example.com", 2L, "Hola", "Mensaje")
        );
        verify(messagePersistencePort, never()).save(any(Message.class));
    }

    @Test
    void createOwnMessageThrowsWhenSenderAndReceiverAreTheSameUser() {
        User sender = user(1L, "driver", true);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(sender));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(sender));

        assertThrows(
                IllegalArgumentException.class,
                () -> messageService.createOwnMessage("driver@example.com", 1L, "Hola", "Mensaje")
        );
        verify(messagePersistencePort, never()).save(any(Message.class));
    }

    @Test
    void markOwnMessageAsReadMarksMessageWhenAuthenticatedUserIsReceiver() {
        User receiver = user(2L, "receiver", true);
        Message message = message(8L, user(1L, "sender", true), receiver);
        message.setIsRead(false);

        when(userPersistencePort.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(receiver));
        when(messagePersistencePort.findById(8L)).thenReturn(Optional.of(message));
        when(messagePersistencePort.save(message)).thenReturn(message);

        Message updatedMessage = messageService.markOwnMessageAsRead("receiver@example.com", 8L);

        assertTrue(updatedMessage.getIsRead());
        verify(messagePersistencePort).save(message);
    }

    @Test
    void markOwnMessageAsReadThrowsWhenAuthenticatedUserIsNotReceiver() {
        User authenticatedUser = user(2L, "driver", true);
        User actualReceiver = user(3L, "receiver", true);
        Message message = message(8L, user(1L, "sender", true), actualReceiver);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(authenticatedUser));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(authenticatedUser));
        when(messagePersistencePort.findById(8L)).thenReturn(Optional.of(message));

        assertThrows(
                IllegalArgumentException.class,
                () -> messageService.markOwnMessageAsRead("driver@example.com", 8L)
        );
        verify(messagePersistencePort, never()).save(message);
    }

    @Test
    void markOwnMessageAsReadThrowsWhenMessageDoesNotExist() {
        User receiver = user(2L, "receiver", true);

        when(userPersistencePort.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(messagePersistencePort.findById(8L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> messageService.markOwnMessageAsRead("receiver@example.com", 8L)
        );
    }

    @Test
    void getAvailableRecipientsFiltersCurrentUserAndDisabledAccountsAndSortsByDisplayName() {
        User currentUser = user(2L, "driver", true);
        User alpha = user(1L, "Alpha", true);
        User zeta = user(3L, "zeta", true);
        User disabled = user(4L, "blocked", false);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(currentUser));
        when(userPersistencePort.findAll()).thenReturn(List.of(zeta, currentUser, disabled, alpha));

        List<User> recipients = messageService.getAvailableRecipients("driver@example.com");

        assertEquals(List.of(alpha, zeta), recipients);
    }

    @Test
    void getOwnMessagesThrowsWhenAuthenticatedUserDoesNotExist() {
        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.getOwnMessages("driver@example.com"));
    }

    private User user(Long id, String displayName, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName.toLowerCase() + "@example.com");
        user.setEnabled(enabled);
        return user;
    }

    private Message message(Long id, User sender, User receiver) {
        Message message = new Message();
        message.setId(id);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject("Hola");
        message.setContent("Mensaje");
        return message;
    }
}
