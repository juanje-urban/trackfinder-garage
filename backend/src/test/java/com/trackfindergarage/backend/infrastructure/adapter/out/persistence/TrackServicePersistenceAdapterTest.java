package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.TrackService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrackServicePersistenceAdapterTest {

    private final SpringDataTrackServiceRepository repository = mock(SpringDataTrackServiceRepository.class);
    private final TrackServicePersistenceAdapter adapter = new TrackServicePersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        TrackService trackService = new TrackService();
        when(repository.save(trackService)).thenReturn(trackService);

        TrackService saved = adapter.save(trackService);

        assertSame(trackService, saved);
        verify(repository).save(trackService);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        TrackService trackService = new TrackService();
        List<TrackService> assignments = List.of(trackService);

        when(repository.findById(1L)).thenReturn(Optional.of(trackService));
        when(repository.findAll()).thenReturn(assignments);
        when(repository.findByTrackId(2L)).thenReturn(assignments);
        when(repository.findByServiceId(3L)).thenReturn(assignments);
        when(repository.findByTrackIdAndServiceId(2L, 3L)).thenReturn(Optional.of(trackService));

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(assignments, adapter.findAll());
        assertEquals(assignments, adapter.findByTrackId(2L));
        assertEquals(assignments, adapter.findByServiceId(3L));
        assertTrue(adapter.findByTrackIdAndServiceId(2L, 3L).isPresent());
    }

    @Test
    void deleteDelegatesToRepository() {
        TrackService trackService = new TrackService();

        adapter.delete(trackService);

        verify(repository).delete(trackService);
    }
}
