package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerServiceServiceTest {

    @Mock
    private OrganizerServicePersistencePort organizerServicePersistencePort;

    @Mock
    private OrganizerPersistencePort organizerPersistencePort;

    @Mock
    private ServicePersistencePort servicePersistencePort;

    @InjectMocks
    private OrganizerServiceService organizerServiceService;

    @Test
    void createOrganizerServicePersistsWhenServiceIsAllowedForOrganizer() {
        OrganizerService organizerService = organizerServiceWithIds(1L, 2L);
        Organizer organizer = organizerWithId(1L);
        com.trackfindergarage.backend.domain.model.Service service = serviceWithId(2L);
        service.setAllowedForOrganizer(true);

        when(organizerServicePersistencePort.findByOrganizerIdUserAndServiceId(1L, 2L)).thenReturn(Optional.empty());
        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.of(organizer));
        when(servicePersistencePort.findById(2L)).thenReturn(Optional.of(service));
        when(organizerServicePersistencePort.save(organizerService)).thenReturn(organizerService);

        OrganizerService created = organizerServiceService.createOrganizerService(organizerService);

        assertSame(organizerService, created);
        assertSame(organizer, organizerService.getOrganizer());
        assertSame(service, organizerService.getService());
        verify(organizerServicePersistencePort).save(organizerService);
    }

    @Test
    void createOrganizerServiceThrowsWhenDuplicateExists() {
        OrganizerService organizerService = organizerServiceWithIds(1L, 2L);

        when(organizerServicePersistencePort.findByOrganizerIdUserAndServiceId(1L, 2L))
                .thenReturn(Optional.of(organizerService));

        assertThrows(DuplicateResourceException.class, () -> organizerServiceService.createOrganizerService(organizerService));
        verify(organizerServicePersistencePort, never()).save(organizerService);
    }

    @Test
    void createOrganizerServiceThrowsWhenServiceIsNotAllowedForOrganizer() {
        OrganizerService organizerService = organizerServiceWithIds(1L, 2L);
        Organizer organizer = organizerWithId(1L);
        com.trackfindergarage.backend.domain.model.Service service = serviceWithId(2L);
        service.setAllowedForOrganizer(false);

        when(organizerServicePersistencePort.findByOrganizerIdUserAndServiceId(1L, 2L)).thenReturn(Optional.empty());
        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.of(organizer));
        when(servicePersistencePort.findById(2L)).thenReturn(Optional.of(service));

        assertThrows(IllegalArgumentException.class, () -> organizerServiceService.createOrganizerService(organizerService));
        verify(organizerServicePersistencePort, never()).save(organizerService);
    }

    @Test
    void deleteOrganizerServiceRemovesExistingAssignment() {
        OrganizerService organizerService = organizerServiceWithIds(1L, 2L);
        organizerService.setId(3L);

        when(organizerServicePersistencePort.findById(3L)).thenReturn(Optional.of(organizerService));

        organizerServiceService.deleteOrganizerService(3L);

        verify(organizerServicePersistencePort).delete(organizerService);
    }

    @Test
    void getAllOrganizerServicesReturnsPersistenceResult() {
        List<OrganizerService> assignments = List.of(organizerServiceWithIds(1L, 2L));

        when(organizerServicePersistencePort.findAll()).thenReturn(assignments);

        assertEquals(assignments, organizerServiceService.getAllOrganizerServices());
    }

    @Test
    void getOrganizerServicesByOrganizerIdThrowsWhenOrganizerDoesNotExist() {
        when(organizerPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> organizerServiceService.getOrganizerServicesByOrganizerId(99L));
    }

    private OrganizerService organizerServiceWithIds(Long organizerId, Long serviceId) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(organizerId);

        com.trackfindergarage.backend.domain.model.Service service = new com.trackfindergarage.backend.domain.model.Service();
        service.setId(serviceId);

        OrganizerService organizerService = new OrganizerService();
        organizerService.setOrganizer(organizer);
        organizerService.setService(service);
        return organizerService;
    }

    private Organizer organizerWithId(Long id) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(id);
        organizer.setLegalName("Legal");
        return organizer;
    }

    private com.trackfindergarage.backend.domain.model.Service serviceWithId(Long id) {
        com.trackfindergarage.backend.domain.model.Service service = new com.trackfindergarage.backend.domain.model.Service();
        service.setId(id);
        service.setName("Service");
        return service;
    }
}
