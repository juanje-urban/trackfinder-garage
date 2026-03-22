package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServicePersistenceAdapterTest {

    private final SpringDataEventServiceRepository repository = mock(SpringDataEventServiceRepository.class);
    private final EventServicePersistenceAdapter adapter = new EventServicePersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        EventService eventService = new EventService();
        when(repository.save(eventService)).thenReturn(eventService);

        EventService saved = adapter.save(eventService);

        assertSame(eventService, saved);
        verify(repository).save(eventService);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        EventService eventService = new EventService();
        List<EventService> eventServices = List.of(eventService);

        when(repository.findById(1L)).thenReturn(Optional.of(eventService));
        when(repository.findAll()).thenReturn(eventServices);
        when(repository.findByEventId(2L)).thenReturn(eventServices);
        when(repository.findByEventIdAndTrackServiceId(2L, 3L)).thenReturn(Optional.of(eventService));
        when(repository.findByEventIdAndOrganizerServiceId(2L, 4L)).thenReturn(Optional.of(eventService));

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(eventServices, adapter.findAll());
        assertEquals(eventServices, adapter.findByEventId(2L));
        assertTrue(adapter.findByEventIdAndTrackServiceId(2L, 3L).isPresent());
        assertTrue(adapter.findByEventIdAndOrganizerServiceId(2L, 4L).isPresent());
    }

    @Test
    void deleteDelegatesToRepository() {
        EventService eventService = new EventService();

        adapter.delete(eventService);

        verify(repository).delete(eventService);
    }
}
