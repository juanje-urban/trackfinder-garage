package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.MessagePersistencePort;
import com.trackfindergarage.backend.domain.model.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto de mensajes con Spring Data JPA.
 *
 * <p>Encapsula el acceso al repositorio de mensajería y ofrece a la aplicación una interfaz estable
 * para guardar mensajes y consultar conversaciones visibles.</p>
 */
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
