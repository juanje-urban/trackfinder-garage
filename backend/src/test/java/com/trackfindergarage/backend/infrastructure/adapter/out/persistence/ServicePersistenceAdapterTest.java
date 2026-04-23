package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Service;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicePersistenceAdapterTest {

    private final SpringDataServiceRepository repository = mock(SpringDataServiceRepository.class);
    private final ServicePersistenceAdapter adapter = new ServicePersistenceAdapter(repository);

    @Test
    void saveDelegatesToRepository() {
        Service service = new Service();
        when(repository.save(service)).thenReturn(service);

        Service saved = adapter.save(service);

        assertSame(service, saved);
        verify(repository).save(service);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Service service = new Service();
        List<Service> services = List.of(service);

        when(repository.findById(1L)).thenReturn(Optional.of(service));
        when(repository.findByName("Parking")).thenReturn(Optional.of(service));
        when(repository.findAll()).thenReturn(services);
        when(repository.findAllByAllowedForOrganizerTrue()).thenReturn(services);

        assertTrue(adapter.findById(1L).isPresent());
        assertTrue(adapter.findByName("Parking").isPresent());
        assertEquals(services, adapter.findAll());
        assertEquals(services, adapter.findAllByAllowedForOrganizerTrue());
    }
}
