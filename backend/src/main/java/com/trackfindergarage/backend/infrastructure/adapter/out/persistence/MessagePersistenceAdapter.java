package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.MessagePersistencePort;
import com.trackfindergarage.backend.domain.model.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MessagePersistenceAdapter implements MessagePersistencePort {

    private final SpringDataMessageRepository messageRepository;

    public MessagePersistenceAdapter(SpringDataMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    @Override
    public Optional<Message> findById(Long id) {
        return messageRepository.findById(id);
    }

    @Override
    public java.util.List<Message> findByParticipantIdOrderBySentAtAsc(Long userId) {
        return messageRepository.findByParticipantIdOrderBySentAtAsc(userId);
    }
}
