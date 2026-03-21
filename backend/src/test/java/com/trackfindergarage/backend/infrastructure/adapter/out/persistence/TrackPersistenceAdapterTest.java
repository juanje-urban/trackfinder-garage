package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Track;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrackPersistenceAdapterTest {

    private final SpringDataTrackRepository repository = mock(SpringDataTrackRepository.class);
    private final TrackPersistenceAdapter adapter = new TrackPersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        Track track = new Track();
        when(repository.save(track)).thenReturn(track);

        Track saved = adapter.save(track);

        assertSame(track, saved);
        verify(repository).save(track);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Track track = new Track();
        List<Track> tracks = List.of(track);

        when(repository.findById(1L)).thenReturn(Optional.of(track));
        when(repository.findAll()).thenReturn(tracks);
        when(repository.existsById(1L)).thenReturn(true);

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(tracks, adapter.findAll());
        assertTrue(adapter.existsById(1L));
    }

    @Test
    void deleteDelegatesToRepository() {
        Track track = new Track();

        adapter.delete(track);

        verify(repository).delete(track);
    }
}
