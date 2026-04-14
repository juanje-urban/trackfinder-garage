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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void createMessagePersistsWhenDataIsValid() {
        Message message = messageWithSenderAndReceiver(1L, 2L);
        User sender = userWithId(1L, "sender");
        User receiver = userWithId(2L, "receiver");

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(sender));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(receiver));
        when(messagePersistencePort.save(message)).thenReturn(message);

        Message created = messageService.createMessage(message);

        assertSame(message, created);
        assertSame(sender, message.getSender());
        assertSame(receiver, message.getReceiver());
        assertFalse(message.getIsRead());
        verify(messagePersistencePort).save(message);
    }

    @Test
    void createMessageThrowsWhenSenderAndReceiverAreTheSame() {
        Message message = messageWithSenderAndReceiver(1L, 1L);

        assertThrows(IllegalArgumentException.class, () -> messageService.createMessage(message));
    }

    @Test
    void createMessageThrowsWhenSenderDoesNotExist() {
        Message message = messageWithSenderAndReceiver(1L, 2L);

        when(userPersistencePort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.createMessage(message));
    }

    @Test
    void createMessageThrowsWhenReceiverDoesNotExist() {
        Message message = messageWithSenderAndReceiver(1L, 2L);

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L, "sender")));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.createMessage(message));
    }

    @Test
    void createMessageThrowsWhenSubjectIsBlank() {
        Message message = messageWithSenderAndReceiver(1L, 2L);
        message.setSubject(" ");

        assertThrows(IllegalArgumentException.class, () -> messageService.createMessage(message));
    }

    @Test
    void createMessageThrowsWhenBodyIsBlank() {
        Message message = messageWithSenderAndReceiver(1L, 2L);
        message.setContent(" ");

        assertThrows(IllegalArgumentException.class, () -> messageService.createMessage(message));
    }

    @Test
    void getAllMessagesReturnsPersistenceResult() {
        List<Message> messages = List.of(messageWithId(5L, 1L, 2L));

        when(messagePersistencePort.findAllByOrderBySentAtAsc()).thenReturn(messages);

        assertEquals(messages, messageService.getAllMessages());
    }

    @Test
    void getMessageByIdThrowsWhenItDoesNotExist() {
        when(messagePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.getMessageById(99L));
    }

    @Test
    void getMessagesBySenderIdReturnsPersistenceResult() {
        List<Message> messages = List.of(messageWithId(5L, 1L, 2L));

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L, "sender")));
        when(messagePersistencePort.findBySenderIdOrderBySentAtAsc(1L)).thenReturn(messages);

        assertEquals(messages, messageService.getMessagesBySenderId(1L));
    }

    @Test
    void getMessagesByReceiverIdReturnsPersistenceResult() {
        List<Message> messages = List.of(messageWithId(5L, 1L, 2L));

        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(userWithId(2L, "receiver")));
        when(messagePersistencePort.findByReceiverIdOrderBySentAtAsc(2L)).thenReturn(messages);

        assertEquals(messages, messageService.getMessagesByReceiverId(2L));
    }

    @Test
    void getConversationReturnsPersistenceResult() {
        List<Message> messages = List.of(messageWithId(5L, 1L, 2L), messageWithId(6L, 2L, 1L));

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L, "sender")));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(userWithId(2L, "receiver")));
        when(messagePersistencePort.findConversation(1L, 2L)).thenReturn(messages);

        assertEquals(messages, messageService.getConversation(1L, 2L));
    }

    @Test
    void getConversationThrowsWhenUsersAreEqual() {
        assertThrows(IllegalArgumentException.class, () -> messageService.getConversation(1L, 1L));
    }

    @Test
    void getOwnMessagesReturnsMessagesForAuthenticatedUser() {
        User currentUser = userWithId(1L, "sender");
        currentUser.setEmail("sender@example.com");
        currentUser.setEnabled(true);
        List<Message> messages = List.of(messageWithId(5L, 1L, 2L), messageWithId(6L, 2L, 1L));

        when(userPersistencePort.findByEmail("sender@example.com")).thenReturn(Optional.of(currentUser));
        when(messagePersistencePort.findByParticipantIdOrderBySentAtAsc(1L)).thenReturn(messages);

        assertEquals(messages, messageService.getOwnMessages("sender@example.com"));
    }

    @Test
    void createOwnMessageUsesAuthenticatedSenderAndPersists() {
        User sender = userWithId(1L, "sender");
        sender.setEmail("sender@example.com");
        sender.setEnabled(true);
        User receiver = userWithId(2L, "receiver");
        receiver.setEnabled(true);

        when(userPersistencePort.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(receiver));
        when(messagePersistencePort.save(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message created = messageService.createOwnMessage("sender@example.com", 2L, "Hola", "Que tal");

        assertEquals(1L, created.getSender().getId());
        assertEquals(2L, created.getReceiver().getId());
        assertEquals("Hola", created.getSubject());
        assertEquals("Que tal", created.getContent());
        assertFalse(created.getIsRead());
    }

    @Test
    void createOwnMessageThrowsWhenReceiverIsDisabled() {
        User sender = userWithId(1L, "sender");
        sender.setEmail("sender@example.com");
        sender.setEnabled(true);
        User receiver = userWithId(2L, "receiver");
        receiver.setEnabled(false);

        when(userPersistencePort.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(receiver));

        assertThrows(IllegalArgumentException.class,
                () -> messageService.createOwnMessage("sender@example.com", 2L, "Hola", "Que tal"));
    }

    @Test
    void getAvailableRecipientsReturnsEnabledUsersExceptCurrentOne() {
        User currentUser = userWithId(1L, "sender");
        currentUser.setEmail("sender@example.com");
        currentUser.setEnabled(true);

        User alpha = userWithId(2L, "Alpha");
        alpha.setEnabled(true);
        User disabled = userWithId(3L, "Disabled");
        disabled.setEnabled(false);
        User zulu = userWithId(4L, "Zulu");
        zulu.setEnabled(true);

        when(userPersistencePort.findByEmail("sender@example.com")).thenReturn(Optional.of(currentUser));
        when(userPersistencePort.findAll()).thenReturn(List.of(zulu, currentUser, disabled, alpha));

        List<User> recipients = messageService.getAvailableRecipients("sender@example.com");

        assertEquals(List.of("Alpha", "Zulu"), recipients.stream().map(User::getDisplayName).toList());
    }

    @Test
    void markOwnMessageAsReadUsesAuthenticatedReceiver() {
        User receiver = userWithId(2L, "receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setEnabled(true);
        Message message = messageWithId(5L, 1L, 2L);
        message.setIsRead(false);

        when(userPersistencePort.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(messagePersistencePort.findById(5L)).thenReturn(Optional.of(message));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(receiver));
        when(messagePersistencePort.save(message)).thenReturn(message);

        Message updated = messageService.markOwnMessageAsRead("receiver@example.com", 5L);

        assertTrue(updated.getIsRead());
    }

    @Test
    void markAsReadPersistsWhenUserIsReceiver() {
        Message message = messageWithId(5L, 1L, 2L);
        message.setIsRead(false);

        when(messagePersistencePort.findById(5L)).thenReturn(Optional.of(message));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(userWithId(2L, "receiver")));
        when(messagePersistencePort.save(message)).thenReturn(message);

        Message updated = messageService.markAsRead(5L, 2L);

        assertSame(message, updated);
        assertTrue(message.getIsRead());
        verify(messagePersistencePort).save(message);
    }

    @Test
    void markAsUnreadPersistsWhenUserIsReceiver() {
        Message message = messageWithId(5L, 1L, 2L);
        message.setIsRead(true);

        when(messagePersistencePort.findById(5L)).thenReturn(Optional.of(message));
        when(userPersistencePort.findById(2L)).thenReturn(Optional.of(userWithId(2L, "receiver")));
        when(messagePersistencePort.save(message)).thenReturn(message);

        Message updated = messageService.markAsUnread(5L, 2L);

        assertSame(message, updated);
        assertFalse(message.getIsRead());
        verify(messagePersistencePort).save(message);
    }

    @Test
    void markAsReadThrowsWhenUserIsNotReceiver() {
        Message message = messageWithId(5L, 1L, 2L);

        when(messagePersistencePort.findById(5L)).thenReturn(Optional.of(message));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L, "sender")));

        assertThrows(IllegalArgumentException.class, () -> messageService.markAsRead(5L, 1L));
    }

    private Message messageWithSenderAndReceiver(Long senderId, Long receiverId) {
        Message message = new Message();
        message.setSender(userWithId(senderId, "sender-" + senderId));
        message.setReceiver(userWithId(receiverId, "receiver-" + receiverId));
        message.setSubject("Subject");
        message.setContent("Body");
        return message;
    }

    private Message messageWithId(Long id, Long senderId, Long receiverId) {
        Message message = messageWithSenderAndReceiver(senderId, receiverId);
        message.setId(id);
        message.setSentAt(LocalDateTime.of(2026, 3, 22, 10, 0));
        message.setIsRead(false);
        return message;
    }

    private User userWithId(Long id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        return user;
    }
}
