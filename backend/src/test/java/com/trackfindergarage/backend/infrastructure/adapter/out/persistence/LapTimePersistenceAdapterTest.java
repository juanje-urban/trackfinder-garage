package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.LapTime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LapTimePersistenceAdapterTest {

    private final SpringDataLapTimeRepository repository = mock(SpringDataLapTimeRepository.class);
    private final LapTimePersistenceAdapter adapter = new LapTimePersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        LapTime lapTime = new LapTime();
        when(repository.save(lapTime)).thenReturn(lapTime);

        LapTime saved = adapter.save(lapTime);

        assertSame(lapTime, saved);
        verify(repository).save(lapTime);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        LapTime lapTime = new LapTime();
        List<LapTime> lapTimes = List.of(lapTime);

        when(repository.findById(1L)).thenReturn(Optional.of(lapTime));
        when(repository.findByUserId(2L)).thenReturn(lapTimes);
        when(repository.findByTrackId(3L)).thenReturn(lapTimes);

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(lapTimes, adapter.findByUserId(2L));
        assertEquals(lapTimes, adapter.findByTrackId(3L));
    }

    @Test
    void deleteDelegatesToRepository() {
        LapTime lapTime = new LapTime();

        adapter.delete(lapTime);

        verify(repository).delete(lapTime);
    }
}
