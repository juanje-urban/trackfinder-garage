package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Message;

import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar mensajes entre usuarios.
 *
 * <p>Proporciona a la capa de aplicación las operaciones mínimas para guardar mensajes y recuperar
 * el histórico visible de un participante.</p>
 */
public interface MessagePersistencePort {

    Message save(Message message);

    Optional<Message> findById(Long id);

    java.util.List<Message> findByParticipantIdOrderBySentAtAsc(Long userId);
}
