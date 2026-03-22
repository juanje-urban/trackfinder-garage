package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataMessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByOrderBySentAtAsc();

    List<Message> findBySenderIdOrderBySentAtAsc(Long senderId);

    List<Message> findByReceiverIdOrderBySentAtAsc(Long receiverId);

    @Query("""
            SELECT m
            FROM Message m
            WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
               OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
            ORDER BY m.sentAt ASC
            """)
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
