package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventBooking;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventBookingPersistenceAdapterTest {

    private final SpringDataEventBookingRepository repository = mock(SpringDataEventBookingRepository.class);
    private final EventBookingPersistenceAdapter adapter = new EventBookingPersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        EventBooking eventBooking = new EventBooking();
        when(repository.save(eventBooking)).thenReturn(eventBooking);

        EventBooking saved = adapter.save(eventBooking);

        assertSame(eventBooking, saved);
        verify(repository).save(eventBooking);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        EventBooking eventBooking = new EventBooking();
        List<EventBooking> eventBookings = List.of(eventBooking);

        when(repository.findById(1L)).thenReturn(Optional.of(eventBooking));
        when(repository.findByUserId(2L)).thenReturn(eventBookings);
        when(repository.findByEventId(3L)).thenReturn(eventBookings);
        when(repository.countByEventId(3L)).thenReturn(2L);
        when(repository.findByUserIdAndEventId(2L, 3L)).thenReturn(Optional.of(eventBooking));

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(eventBookings, adapter.findByUserId(2L));
        assertEquals(eventBookings, adapter.findByEventId(3L));
        assertEquals(2L, adapter.countByEventId(3L));
        assertTrue(adapter.findByUserIdAndEventId(2L, 3L).isPresent());
    }

    @Test
    void deleteDelegatesToRepository() {
        EventBooking eventBooking = new EventBooking();

        adapter.delete(eventBooking);

        verify(repository).delete(eventBooking);
    }
}
