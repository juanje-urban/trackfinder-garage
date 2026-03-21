package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Organizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizerPersistenceAdapterTest {

    private final SpringDataOrganizerRepository repository = mock(SpringDataOrganizerRepository.class);
    private final OrganizerPersistenceAdapter adapter = new OrganizerPersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        Organizer organizer = new Organizer();
        when(repository.save(organizer)).thenReturn(organizer);

        Organizer saved = adapter.save(organizer);

        assertSame(organizer, saved);
        verify(repository).save(organizer);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Organizer organizer = new Organizer();
        List<Organizer> organizers = List.of(organizer);

        when(repository.findById(1L)).thenReturn(Optional.of(organizer));
        when(repository.findAll()).thenReturn(organizers);
        when(repository.findByLegalName("Legal")).thenReturn(Optional.of(organizer));
        when(repository.findByCif("B12345678")).thenReturn(Optional.of(organizer));

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(organizers, adapter.findAll());
        assertTrue(adapter.findByLegalName("Legal").isPresent());
        assertTrue(adapter.findByCif("B12345678").isPresent());
    }

    @Test
    void deleteDelegatesToRepository() {
        Organizer organizer = new Organizer();

        adapter.delete(organizer);

        verify(repository).delete(organizer);
    }
}
