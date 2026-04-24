package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageContactResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.MessageResponse;
import org.springframework.stereotype.Component;

@Component
public class MessageWebMapper {

    public MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderDisplayName(message.getSender() != null ? message.getSender().getDisplayName() : null)
                .receiverId(message.getReceiver() != null ? message.getReceiver().getId() : null)
                .receiverDisplayName(message.getReceiver() != null ? message.getReceiver().getDisplayName() : null)
                .sentAt(message.getSentAt())
                .isRead(message.getIsRead())
                .subject(message.getSubject())
                .message(message.getContent())
                .build();
    }

    public MessageContactResponse toContactResponse(User user) {
        return MessageContactResponse.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .build();
    }
}
