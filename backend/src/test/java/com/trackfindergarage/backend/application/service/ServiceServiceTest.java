package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.domain.model.Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServicePersistencePort servicePersistencePort;

    @InjectMocks
    private ServiceService serviceService;

    @Test
    void createServiceSavesWithEnabledTrueWhenNameDoesNotExist() {
        Service service = serviceWithId(1L, "Parking");
        service.setEnabled(false);

        when(servicePersistencePort.findByName("Parking")).thenReturn(Optional.empty());
        when(servicePersistencePort.save(service)).thenReturn(service);

        Service createdService = serviceService.createService(service);

        assertSame(service, createdService);
        assertTrue(createdService.getEnabled());
        verify(servicePersistencePort).save(service);
    }

    @Test
    void createServiceThrowsWhenNameAlreadyExists() {
        Service service = serviceWithId(1L, "Parking");

        when(servicePersistencePort.findByName("Parking")).thenReturn(Optional.of(service));

        assertThrows(DuplicateResourceException.class, () -> serviceService.createService(service));
        verify(servicePersistencePort, never()).save(service);
    }

    @Test
    void updateServiceCopiesMutableFieldsAndSaves() {
        Service existingService = serviceWithId(5L, "Parking");
        existingService.setDescription("Old description");
        existingService.setAllowedForTrack(false);
        existingService.setAllowedForOrganizer(false);
        existingService.setEnabled(true);

        Service updateRequest = serviceWithId(null, "Box");
        updateRequest.setDescription("New description");
        updateRequest.setAllowedForTrack(true);
        updateRequest.setAllowedForOrganizer(true);
        updateRequest.setEnabled(false);

        when(servicePersistencePort.findById(5L)).thenReturn(Optional.of(existingService));
        when(servicePersistencePort.findByName("Box")).thenReturn(Optional.empty());
        when(servicePersistencePort.save(existingService)).thenReturn(existingService);

        Service updatedService = serviceService.updateService(5L, updateRequest);

        assertSame(existingService, updatedService);
        assertEquals("Box", existingService.getName());
        assertEquals("New description", existingService.getDescription());
        assertTrue(existingService.getAllowedForTrack());
        assertTrue(existingService.getAllowedForOrganizer());
        assertTrue(existingService.getEnabled());
        verify(servicePersistencePort).save(existingService);
    }

    @Test
    void updateServiceThrowsWhenAnotherServiceUsesSameName() {
        Service existingService = serviceWithId(5L, "Parking");
        Service otherService = serviceWithId(9L, "Box");
        Service updateRequest = serviceWithId(null, "Box");

        when(servicePersistencePort.findById(5L)).thenReturn(Optional.of(existingService));
        when(servicePersistencePort.findByName("Box")).thenReturn(Optional.of(otherService));

        assertThrows(DuplicateResourceException.class, () -> serviceService.updateService(5L, updateRequest));
        verify(servicePersistencePort, never()).save(existingService);
    }

    @Test
    void getAllServicesReturnsPersistenceResult() {
        List<Service> services = List.of(serviceWithId(1L, "Parking"), serviceWithId(2L, "Box"));

        when(servicePersistencePort.findAll()).thenReturn(services);

        assertEquals(services, serviceService.getAllServices());
    }

    @Test
    void enableServiceMarksServiceAsEnabled() {
        Service existingService = serviceWithId(4L, "Parking");
        existingService.setEnabled(false);

        when(servicePersistencePort.findById(4L)).thenReturn(Optional.of(existingService));
        when(servicePersistencePort.save(existingService)).thenReturn(existingService);

        Service enabledService = serviceService.enableService(4L);

        assertTrue(enabledService.getEnabled());
        verify(servicePersistencePort).save(existingService);
    }

    @Test
    void disableServiceMarksServiceAsDisabled() {
        Service existingService = serviceWithId(4L, "Parking");
        existingService.setEnabled(true);

        when(servicePersistencePort.findById(4L)).thenReturn(Optional.of(existingService));
        when(servicePersistencePort.save(existingService)).thenReturn(existingService);

        Service disabledService = serviceService.disableService(4L);

        assertEquals(Boolean.FALSE, disabledService.getEnabled());
        verify(servicePersistencePort).save(existingService);
    }

    private Service serviceWithId(Long id, String name) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        service.setDescription("Description");
        service.setAllowedForTrack(false);
        service.setAllowedForOrganizer(false);
        service.setEnabled(true);
        return service;
    }
}
