package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.ServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateServiceRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceWebMapperTest {

    private final ServiceWebMapper serviceWebMapper = new ServiceWebMapper();

    @Test
    void toDomainMapsCreateRequestToService() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Parking");
        request.setDescription("Covered parking");
        request.setAllowedForTrack(true);
        request.setAllowedForOrganizer(false);

        Service service = serviceWebMapper.toDomain(request);

        assertEquals("Parking", service.getName());
        assertEquals("Covered parking", service.getDescription());
        assertEquals(Boolean.TRUE, service.getAllowedForTrack());
        assertEquals(Boolean.FALSE, service.getAllowedForOrganizer());
    }

    @Test
    void updateDomainMapsUpdateRequestToExistingService() {
        Service service = new Service();
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Camping");
        request.setDescription("Overnight stay");
        request.setAllowedForTrack(false);
        request.setAllowedForOrganizer(true);

        serviceWebMapper.updateDomain(service, request);

        assertEquals("Camping", service.getName());
        assertEquals("Overnight stay", service.getDescription());
        assertEquals(Boolean.FALSE, service.getAllowedForTrack());
        assertEquals(Boolean.TRUE, service.getAllowedForOrganizer());
    }

    @Test
    void toResponseMapsServiceToResponse() {
        Service service = new Service();
        service.setId(6L);
        service.setName("Box");
        service.setDescription("Box rental");
        service.setAllowedForTrack(true);
        service.setAllowedForOrganizer(true);
        service.setEnabled(true);

        ServiceResponse response = serviceWebMapper.toResponse(service);

        assertEquals(6L, response.getId());
        assertEquals("Box", response.getName());
        assertEquals("Box rental", response.getDescription());
        assertEquals(Boolean.TRUE, response.getAllowedForTrack());
        assertEquals(Boolean.TRUE, response.getAllowedForOrganizer());
        assertEquals(Boolean.TRUE, response.getEnabled());
    }
}
