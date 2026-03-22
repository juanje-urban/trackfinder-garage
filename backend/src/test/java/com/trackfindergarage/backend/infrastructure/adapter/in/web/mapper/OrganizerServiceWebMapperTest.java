package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerServiceResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrganizerServiceWebMapperTest {

    private final OrganizerServiceWebMapper organizerServiceWebMapper = new OrganizerServiceWebMapper();

    @Test
    void toDomainMapsCreateRequestToOrganizerService() {
        CreateOrganizerServiceRequest request = new CreateOrganizerServiceRequest();
        request.setOrganizerId(1L);
        request.setServiceId(2L);

        OrganizerService organizerService = organizerServiceWebMapper.toDomain(request);

        assertEquals(1L, organizerService.getOrganizer().getIdUser());
        assertEquals(2L, organizerService.getService().getId());
    }

    @Test
    void toResponseMapsOrganizerServiceToResponse() {
        Organizer organizer = new Organizer();
        organizer.setIdUser(1L);
        organizer.setLegalName("Organizer SL");

        com.trackfindergarage.backend.domain.model.Service service =
                new com.trackfindergarage.backend.domain.model.Service();
        service.setId(2L);
        service.setName("Parking");

        OrganizerService organizerService = new OrganizerService();
        organizerService.setId(10L);
        organizerService.setOrganizer(organizer);
        organizerService.setService(service);

        OrganizerServiceResponse response = organizerServiceWebMapper.toResponse(organizerService);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getOrganizerId());
        assertEquals("Organizer SL", response.getOrganizerLegalName());
        assertEquals(2L, response.getServiceId());
        assertEquals("Parking", response.getServiceName());
    }
}
