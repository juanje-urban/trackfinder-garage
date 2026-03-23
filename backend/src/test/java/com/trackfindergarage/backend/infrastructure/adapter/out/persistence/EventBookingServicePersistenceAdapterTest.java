package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventBookingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventBookingServicePersistenceAdapterTest {

    private final SpringDataEventBookingServiceRepository repository = mock(SpringDataEventBookingServiceRepository.class);
    private final EventBookingServicePersistenceAdapter adapter = new EventBookingServicePersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        EventBookingService eventBookingService = new EventBookingService();
        when(repository.save(eventBookingService)).thenReturn(eventBookingService);

        EventBookingService saved = adapter.save(eventBookingService);

        assertSame(eventBookingService, saved);
        verify(repository).save(eventBookingService);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        EventBookingService eventBookingService = new EventBookingService();
        List<EventBookingService> eventBookingServices = List.of(eventBookingService);

        when(repository.findById(1L)).thenReturn(Optional.of(eventBookingService));
        when(repository.findAll()).thenReturn(eventBookingServices);
        when(repository.findByEventBookingId(2L)).thenReturn(eventBookingServices);
        when(repository.findByEventBookingEventId(3L)).thenReturn(eventBookingServices);
        when(repository.findByEventBookingUserId(4L)).thenReturn(eventBookingServices);
        when(repository.findByEventBookingEventIdAndEventBookingUserId(3L, 4L)).thenReturn(eventBookingServices);
        when(repository.findByEventBookingIdAndEventServiceId(2L, 5L)).thenReturn(Optional.of(eventBookingService));

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(eventBookingServices, adapter.findAll());
        assertEquals(eventBookingServices, adapter.findByEventBookingId(2L));
        assertEquals(eventBookingServices, adapter.findByEventBookingEventId(3L));
        assertEquals(eventBookingServices, adapter.findByEventBookingUserId(4L));
        assertEquals(eventBookingServices, adapter.findByEventBookingEventIdAndEventBookingUserId(3L, 4L));
        assertTrue(adapter.findByEventBookingIdAndEventServiceId(2L, 5L).isPresent());
    }

    @Test
    void deleteDelegatesToRepository() {
        EventBookingService eventBookingService = new EventBookingService();

        adapter.delete(eventBookingService);

        verify(repository).delete(eventBookingService);
    }
}
