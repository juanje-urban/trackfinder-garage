package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessagePersistencePort {

    Message save(Message message);

    Optional<Message> findById(Long id);

    List<Message> findAllByOrderBySentAtAsc();

    List<Message> findBySenderIdOrderBySentAtAsc(Long senderId);

    List<Message> findByReceiverIdOrderBySentAtAsc(Long receiverId);

    List<Message> findByParticipantIdOrderBySentAtAsc(Long userId);

    List<Message> findConversation(Long userId1, Long userId2);
}
