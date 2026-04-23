package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Message;

import java.util.Optional;

public interface MessagePersistencePort {

    Message save(Message message);

    Optional<Message> findById(Long id);

    java.util.List<Message> findByParticipantIdOrderBySentAtAsc(Long userId);
}
