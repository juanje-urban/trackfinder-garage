package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.OrganizerService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizerServicePersistenceAdapterTest {

    private final SpringDataOrganizerServiceRepository repository = mock(SpringDataOrganizerServiceRepository.class);
    private final OrganizerServicePersistenceAdapter adapter = new OrganizerServicePersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        OrganizerService organizerService = new OrganizerService();
        when(repository.save(organizerService)).thenReturn(organizerService);

        OrganizerService saved = adapter.save(organizerService);

        assertSame(organizerService, saved);
        verify(repository).save(organizerService);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        OrganizerService organizerService = new OrganizerService();
        List<OrganizerService> assignments = List.of(organizerService);

        when(repository.findById(1L)).thenReturn(Optional.of(organizerService));
        when(repository.findByOrganizerIdUser(2L)).thenReturn(assignments);
        when(repository.findByOrganizerIdUserAndServiceId(2L, 3L)).thenReturn(Optional.of(organizerService));

        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(assignments, adapter.findByOrganizerIdUser(2L));
        assertTrue(adapter.findByOrganizerIdUserAndServiceId(2L, 3L).isPresent());
    }
}
