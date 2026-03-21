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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceControllerTest {

    private final ServiceUseCase serviceUseCase = mock(ServiceUseCase.class);
    private final ServiceWebMapper serviceWebMapper = new ServiceWebMapper();
    private final ServiceController serviceController = new ServiceController(serviceUseCase, serviceWebMapper);

    @Test
    void createServiceDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Parking");
        request.setDescription("Covered");
        request.setAllowedForTrack(true);
        request.setAllowedForOrganizer(false);

        Service created = serviceWithId(1L, "Parking");

        when(serviceUseCase.createService(any(Service.class))).thenReturn(created);

        ServiceResponse response = serviceController.createService(request);

        assertEquals(1L, response.getId());
        assertEquals("Parking", response.getName());
        verify(serviceUseCase).createService(any(Service.class));
    }

    @Test
    void updateServiceDelegatesToUseCaseAndReturnsMappedResponse() {
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setName("Box");
        request.setDescription("Rental");
        request.setAllowedForTrack(true);
        request.setAllowedForOrganizer(true);

        Service updated = serviceWithId(2L, "Box");

        when(serviceUseCase.updateService(eq(2L), any(Service.class))).thenReturn(updated);

        ServiceResponse response = serviceController.updateService(2L, request);

        assertEquals(2L, response.getId());
        assertEquals("Box", response.getName());
        verify(serviceUseCase).updateService(eq(2L), any(Service.class));
    }

    @Test
    void getAllServicesMapsUseCaseResult() {
        when(serviceUseCase.getAllServices()).thenReturn(List.of(serviceWithId(1L, "Parking")));

        List<ServiceResponse> response = serviceController.getAllServices();

        assertEquals(1, response.size());
        assertEquals("Parking", response.getFirst().getName());
    }

    @Test
    void filteredEndpointsMapUseCaseResult() {
        when(serviceUseCase.getAllServicesAllowedForTrack()).thenReturn(List.of(serviceWithId(1L, "Parking")));
        when(serviceUseCase.getAllServicesAllowedForOrganizer()).thenReturn(List.of(serviceWithId(2L, "Camping")));

        List<ServiceResponse> trackServices = serviceController.getAllServicesAllowedForTrack();
        List<ServiceResponse> organizerServices = serviceController.getAllServicesAllowedForOrganizer();

        assertEquals("Parking", trackServices.getFirst().getName());
        assertEquals("Camping", organizerServices.getFirst().getName());
    }

    @Test
    void getByIdAndEnableDisableReturnMappedResponses() {
        Service service = serviceWithId(3L, "Box");
        Service enabled = serviceWithId(3L, "Box");
        enabled.setEnabled(true);
        Service disabled = serviceWithId(3L, "Box");
        disabled.setEnabled(false);

        when(serviceUseCase.getServiceById(3L)).thenReturn(service);
        when(serviceUseCase.enableService(3L)).thenReturn(enabled);
        when(serviceUseCase.disableService(3L)).thenReturn(disabled);

        ServiceResponse byId = serviceController.getServiceById(3L);
        ServiceResponse enabledResponse = serviceController.enableService(3L);
        ServiceResponse disabledResponse = serviceController.disableService(3L);

        assertEquals(3L, byId.getId());
        assertTrue(enabledResponse.getEnabled());
        assertEquals(Boolean.FALSE, disabledResponse.getEnabled());
    }

    @Test
    void deleteServiceDelegatesToUseCase() {
        serviceController.deleteService(4L);

        verify(serviceUseCase).deleteService(4L);
    }

    private Service serviceWithId(Long id, String name) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        service.setDescription("Description");
        service.setAllowedForTrack(true);
        service.setAllowedForOrganizer(true);
        service.setEnabled(true);
        return service;
    }
}
