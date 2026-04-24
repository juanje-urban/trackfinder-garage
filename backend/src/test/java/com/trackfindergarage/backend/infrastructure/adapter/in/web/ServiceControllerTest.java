package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.ServiceUseCase;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.ServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.ServiceWebMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceControllerTest {

    private final ServiceUseCase serviceUseCase = mock(ServiceUseCase.class);
    private final ServiceWebMapper serviceWebMapper = new ServiceWebMapper();
    private final ServiceController serviceController = new ServiceController(serviceUseCase, serviceWebMapper);

    @Test
    void createServiceDelegatesWithMappedDomainObject() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Coaching");
        request.setDescription("Briefing");
        request.setAllowedForTrack(true);
        request.setAllowedForOrganizer(false);

        when(serviceUseCase.createService(any(Service.class))).thenReturn(service(1L, "Coaching", true, false, true));

        ServiceResponse response = serviceController.createService(request);

        assertEquals(1L, response.getId());
        verify(serviceUseCase).createService(argThat(service ->
                "Coaching".equals(service.getName())
                        && "Briefing".equals(service.getDescription())
                        && Boolean.TRUE.equals(service.getAllowedForTrack())
                        && Boolean.FALSE.equals(service.getAllowedForOrganizer())
        ));
    }

    @Test
    void updateServiceDelegatesWithMappedDomainObject() {
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Tyres");
        request.setDescription("Premium tyres");
        request.setAllowedForTrack(false);
        request.setAllowedForOrganizer(true);

        when(serviceUseCase.updateService(any(Long.class), any(Service.class)))
                .thenReturn(service(2L, "Tyres", false, true, true));

        ServiceResponse response = serviceController.updateService(2L, request);

        assertEquals("Tyres", response.getName());
        verify(serviceUseCase).updateService(argThat(id -> id.equals(2L)), argThat(service ->
                "Tyres".equals(service.getName())
                        && "Premium tyres".equals(service.getDescription())
                        && Boolean.FALSE.equals(service.getAllowedForTrack())
                        && Boolean.TRUE.equals(service.getAllowedForOrganizer())
        ));
    }

    @Test
    void listingAndToggleEndpointsMapResponses() {
        when(serviceUseCase.getAllServices()).thenReturn(List.of(service(1L, "A", true, false, true)));
        when(serviceUseCase.enableService(3L)).thenReturn(service(3L, "C", true, true, true));
        when(serviceUseCase.disableService(4L)).thenReturn(service(4L, "D", true, true, false));

        List<ServiceResponse> allServices = serviceController.getAllServices();
        ServiceResponse enabled = serviceController.enableService(3L);
        ServiceResponse disabled = serviceController.disableService(4L);

        assertEquals(1, allServices.size());
        assertEquals("A", allServices.get(0).getName());
        assertEquals(true, enabled.getEnabled());
        assertEquals(false, disabled.getEnabled());
    }

    private Service service(Long id,
                            String name,
                            boolean allowedForTrack,
                            boolean allowedForOrganizer,
                            boolean enabled) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        service.setDescription(name + " description");
        service.setAllowedForTrack(allowedForTrack);
        service.setAllowedForOrganizer(allowedForOrganizer);
        service.setEnabled(enabled);
        return service;
    }
}
