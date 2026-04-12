package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;

import java.util.List;

public interface MessageUseCase {

    Message createMessage(Message message);

    List<Message> getAllMessages();

    Message getMessageById(Long id);

    List<Message> getMessagesBySenderId(Long senderId);

    List<Message> getMessagesByReceiverId(Long receiverId);

    List<Message> getConversation(Long userId1, Long userId2);

    Message markAsRead(Long id, Long userId);

    Message markAsUnread(Long id, Long userId);

    List<Message> getOwnMessages(String authenticatedEmail);

    List<Message> getOwnConversation(String authenticatedEmail, Long counterpartUserId);

    Message createOwnMessage(String authenticatedEmail, Long receiverId, String subject, String content);

    Message markOwnMessageAsRead(String authenticatedEmail, Long id);

    Message markOwnMessageAsUnread(String authenticatedEmail, Long id);

    List<User> getAvailableRecipients(String authenticatedEmail);
}
