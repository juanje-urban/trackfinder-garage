package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Message;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio Spring Data para la entidad {@link Message}.
 *
 * <p>Define las consultas de mensajería cargando remitente y destinatario para que la aplicación no
 * dependa de cargas posteriores.</p>
 */
public interface SpringDataMessageRepository extends JpaRepository<Message, Long> {

    @Override
    @EntityGraph(attributePaths = {"sender", "receiver"})
    java.util.Optional<Message> findById(Long id);

    @EntityGraph(attributePaths = {"sender", "receiver"})
    @Query("""
            SELECT m
            FROM Message m
            WHERE m.sender.id = :userId
               OR m.receiver.id = :userId
            ORDER BY m.sentAt ASC
            """)
    List<Message> findByParticipantIdOrderBySentAtAsc(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"sender", "receiver"})
    @Query("""
            SELECT m
            FROM Message m
            WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
               OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
            ORDER BY m.sentAt ASC
            """)
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
