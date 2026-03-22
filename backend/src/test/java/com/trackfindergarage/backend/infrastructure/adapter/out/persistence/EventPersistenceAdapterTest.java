package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Event;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventPersistenceAdapterTest {

    private final SpringDataEventRepository repository = mock(SpringDataEventRepository.class);
    private final EventPersistenceAdapter adapter = new EventPersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        Event event = new Event();
        when(repository.save(event)).thenReturn(event);

        Event saved = adapter.save(event);

        assertSame(event, saved);
        verify(repository).save(event);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Event event = new Event();
        List<Event> events = List.of(event);
        LocalDate date = LocalDate.now().plusDays(10);

        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(repository.findAll()).thenReturn(events);
        when(repository.findByOrganizerIdUser(2L)).thenReturn(events);
        when(repository.findByTrackId(3L)).thenReturn(events);
        when(repository.findByEventDateBetween(date, date.plusDays(1))).thenReturn(events);
        when(repository.findByTrackIdAndEventDate(3L, date)).thenReturn(Optional.of(event));

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(events, adapter.findAll());
        assertEquals(events, adapter.findByOrganizerIdUser(2L));
        assertEquals(events, adapter.findByTrackId(3L));
        assertEquals(events, adapter.findByEventDateBetween(date, date.plusDays(1)));
        assertTrue(adapter.findByTrackIdAndEventDate(3L, date).isPresent());
    }

    @Test
    void deleteDelegatesToRepository() {
        Event event = new Event();

        adapter.delete(event);

        verify(repository).delete(event);
    }
}
