package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.MessagePersistencePort;
import com.trackfindergarage.backend.domain.model.Message;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public List<Message> findAllByOrderBySentAtAsc() {
        return messageRepository.findAllByOrderBySentAtAsc();
    }

    @Override
    public List<Message> findBySenderIdOrderBySentAtAsc(Long senderId) {
        return messageRepository.findBySenderIdOrderBySentAtAsc(senderId);
    }

    @Override
    public List<Message> findByReceiverIdOrderBySentAtAsc(Long receiverId) {
        return messageRepository.findByReceiverIdOrderBySentAtAsc(receiverId);
    }

    @Override
    public List<Message> findConversation(Long userId1, Long userId2) {
        return messageRepository.findConversation(userId1, userId2);
    }
}
