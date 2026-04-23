package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventBookingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

        when(repository.findByEventBookingId(2L)).thenReturn(eventBookingServices);
        when(repository.findByEventBookingEventId(3L)).thenReturn(eventBookingServices);
        when(repository.findByEventBookingEventIdAndEventBookingUserId(3L, 4L)).thenReturn(eventBookingServices);
        when(repository.findByEventServiceId(5L)).thenReturn(eventBookingServices);

        assertEquals(eventBookingServices, adapter.findByEventBookingId(2L));
        assertEquals(eventBookingServices, adapter.findByEventBookingEventId(3L));
        assertEquals(eventBookingServices, adapter.findByEventBookingEventIdAndEventBookingUserId(3L, 4L));
        assertEquals(eventBookingServices, adapter.findByEventServiceId(5L));
    }

    @Test
    void deleteDelegatesToRepository() {
        EventBookingService eventBookingService = new EventBookingService();

        adapter.delete(eventBookingService);

        verify(repository).delete(eventBookingService);
    }
}
