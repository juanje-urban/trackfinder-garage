package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;

import java.util.List;

public interface MessageUseCase {

    List<Message> getOwnMessages(String authenticatedEmail);

    Message createOwnMessage(String authenticatedEmail, Long receiverId, String subject, String content);

    Message markOwnMessageAsRead(String authenticatedEmail, Long id);

    List<User> getAvailableRecipients(String authenticatedEmail);
}
