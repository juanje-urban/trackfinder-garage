package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponse {

    private Long id;
    private Long senderId;
    private String senderDisplayName;
    private Long receiverId;
    private String receiverDisplayName;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private String subject;
    private String message;
}
