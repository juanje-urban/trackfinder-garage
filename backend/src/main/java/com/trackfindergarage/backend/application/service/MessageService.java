package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.MessageUseCase;
import com.trackfindergarage.backend.application.port.out.MessagePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class MessageService implements MessageUseCase {

    private static final String MESSAGE_NOT_FOUND_WITH_ID = "Message not found with id: ";
    private static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    private static final String SENDER_ID_REQUIRED = "Sender id is required";
    private static final String RECEIVER_ID_REQUIRED = "Receiver id is required";
    private static final String SUBJECT_REQUIRED = "Subject is required";
    private static final String MESSAGE_TEXT_REQUIRED = "Message text is required";
    private static final String SUBJECT_TOO_LONG = "Subject must not be longer than 255 characters";
    private static final String MESSAGE_TEXT_TOO_LONG = "Message text must not be longer than 500 characters";
    private static final String USER_CANNOT_MESSAGE_SELF = "A user cannot send a message to themselves";
    private static final String CONVERSATION_REQUIRES_TWO_DIFFERENT_USERS =
            "Conversation requires two different users";
    private static final String ONLY_RECEIVER_CAN_CHANGE_READ_STATUS =
            "Only the receiver can change the read status of a message";

    private final MessagePersistencePort messagePersistencePort;
    private final UserPersistencePort userPersistencePort;

    public MessageService(MessagePersistencePort messagePersistencePort, UserPersistencePort userPersistencePort) {
        this.messagePersistencePort = messagePersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Message createMessage(Message message) {
        validateMessage(message);

        Long senderId = extractSenderId(message);
        Long receiverId = extractReceiverId(message);

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(USER_CANNOT_MESSAGE_SELF);
        }

        User sender = userPersistencePort.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + senderId));
        User receiver = userPersistencePort.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + receiverId));

        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        return messagePersistencePort.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessages() {
        return messagePersistencePort.findAllByOrderBySentAtAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Message getMessageById(Long id) {
        return findMessageOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessagesBySenderId(Long senderId) {
        userPersistencePort.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + senderId));

        return messagePersistencePort.findBySenderIdOrderBySentAtAsc(senderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessagesByReceiverId(Long receiverId) {
        userPersistencePort.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + receiverId));

        return messagePersistencePort.findByReceiverIdOrderBySentAtAsc(receiverId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getConversation(Long userId1, Long userId2) {
        validateConversationUsers(userId1, userId2);

        userPersistencePort.findById(userId1)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId1));
        userPersistencePort.findById(userId2)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId2));

        return messagePersistencePort.findConversation(userId1, userId2);
    }

    @Override
    public Message markAsRead(Long id, Long userId) {
        Message message = findMessageOrThrow(id);
        validateReceiverAccess(message, userId);

        message.setIsRead(true);
        return messagePersistencePort.save(message);
    }

    @Override
    public Message markAsUnread(Long id, Long userId) {
        Message message = findMessageOrThrow(id);
        validateReceiverAccess(message, userId);

        message.setIsRead(false);
        return messagePersistencePort.save(message);
    }

    private void validateMessage(Message message) {
        if (message.getSender() == null || message.getSender().getId() == null) {
            throw new IllegalArgumentException(SENDER_ID_REQUIRED);
        }
        if (message.getReceiver() == null || message.getReceiver().getId() == null) {
            throw new IllegalArgumentException(RECEIVER_ID_REQUIRED);
        }
        if (message.getSubject() == null || message.getSubject().isBlank()) {
            throw new IllegalArgumentException(SUBJECT_REQUIRED);
        }
        if (message.getSubject().length() > 255) {
            throw new IllegalArgumentException(SUBJECT_TOO_LONG);
        }
        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new IllegalArgumentException(MESSAGE_TEXT_REQUIRED);
        }
        if (message.getContent().length() > 500) {
            throw new IllegalArgumentException(MESSAGE_TEXT_TOO_LONG);
        }
    }

    private void validateConversationUsers(Long userId1, Long userId2) {
        if (userId1 == null) {
            throw new IllegalArgumentException(SENDER_ID_REQUIRED);
        }
        if (userId2 == null) {
            throw new IllegalArgumentException(RECEIVER_ID_REQUIRED);
        }
        if (userId1.equals(userId2)) {
            throw new IllegalArgumentException(CONVERSATION_REQUIRES_TWO_DIFFERENT_USERS);
        }
    }

    private void validateReceiverAccess(Message message, Long userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));

        if (!message.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException(ONLY_RECEIVER_CAN_CHANGE_READ_STATUS);
        }
    }

    private Long extractSenderId(Message message) {
        return message.getSender().getId();
    }

    private Long extractReceiverId(Message message) {
        return message.getReceiver().getId();
    }

    private Message findMessageOrThrow(Long id) {
        return messagePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND_WITH_ID + id));
    }
}
