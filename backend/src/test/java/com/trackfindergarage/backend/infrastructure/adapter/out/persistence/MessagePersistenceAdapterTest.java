package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Message;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessagePersistenceAdapterTest {

    private final SpringDataMessageRepository repository = mock(SpringDataMessageRepository.class);
    private final MessagePersistenceAdapter adapter = new MessagePersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        Message message = new Message();
        when(repository.save(message)).thenReturn(message);

        Message saved = adapter.save(message);

        assertSame(message, saved);
        verify(repository).save(message);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Message message = new Message();
        List<Message> messages = List.of(message);

        when(repository.findById(1L)).thenReturn(Optional.of(message));
        when(repository.findAllByOrderBySentAtAsc()).thenReturn(messages);
        when(repository.findBySenderIdOrderBySentAtAsc(2L)).thenReturn(messages);
        when(repository.findByReceiverIdOrderBySentAtAsc(3L)).thenReturn(messages);
        when(repository.findConversation(2L, 3L)).thenReturn(messages);

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(messages, adapter.findAllByOrderBySentAtAsc());
        assertEquals(messages, adapter.findBySenderIdOrderBySentAtAsc(2L));
        assertEquals(messages, adapter.findByReceiverIdOrderBySentAtAsc(3L));
        assertEquals(messages, adapter.findConversation(2L, 3L));
    }
}
