package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerServiceUseCase;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerServiceWebMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizerServiceControllerTest {

    private final OrganizerServiceUseCase organizerServiceUseCase = mock(OrganizerServiceUseCase.class);
    private final OrganizerServiceWebMapper organizerServiceWebMapper = new OrganizerServiceWebMapper();
    private final OrganizerServiceController organizerServiceController =
            new OrganizerServiceController(organizerServiceUseCase, organizerServiceWebMapper);

    @Test
    void createOrganizerServiceDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateOrganizerServiceRequest request = new CreateOrganizerServiceRequest();
        request.setOrganizerId(1L);
        request.setServiceId(2L);

        when(organizerServiceUseCase.createOrganizerService(any(OrganizerService.class)))
                .thenReturn(organizerServiceWithIds(10L, 1L, 2L));

        OrganizerServiceResponse response = organizerServiceController.createOrganizerService(request);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getOrganizerId());
        assertEquals(2L, response.getServiceId());
    }

    @Test
    void listingEndpointsMapUseCaseResult() {
        OrganizerService organizerService = organizerServiceWithIds(10L, 1L, 2L);

        when(organizerServiceUseCase.getAllOrganizerServices()).thenReturn(List.of(organizerService));
        when(organizerServiceUseCase.getOrganizerServicesByOrganizerId(1L)).thenReturn(List.of(organizerService));
        when(organizerServiceUseCase.getOrganizerServicesByServiceId(2L)).thenReturn(List.of(organizerService));
        when(organizerServiceUseCase.getOrganizerServiceById(10L)).thenReturn(organizerService);

        assertEquals(1, organizerServiceController.getAllOrganizerServices().size());
        assertEquals(1, organizerServiceController.getOrganizerServicesByOrganizerId(1L).size());
        assertEquals(1, organizerServiceController.getOrganizerServicesByServiceId(2L).size());
        assertEquals(10L, organizerServiceController.getOrganizerServiceById(10L).getId());
    }

    @Test
    void deleteOrganizerServiceDelegatesToUseCase() {
        organizerServiceController.deleteOrganizerService(10L);

        verify(organizerServiceUseCase).deleteOrganizerService(10L);
    }

    private OrganizerService organizerServiceWithIds(Long id, Long organizerId, Long serviceId) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(organizerId);
        organizer.setLegalName("Organizer");

        com.trackfindergarage.backend.domain.model.Service service = new com.trackfindergarage.backend.domain.model.Service();
        service.setId(serviceId);
        service.setName("Parking");

        OrganizerService organizerService = new OrganizerService();
        organizerService.setId(id);
        organizerService.setOrganizer(organizer);
        organizerService.setService(service);
        return organizerService;
    }
}
