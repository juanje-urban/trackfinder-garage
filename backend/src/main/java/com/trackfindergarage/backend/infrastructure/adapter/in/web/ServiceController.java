package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.ServiceUseCase;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.ServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.ServiceWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@PreAuthorize("hasRole('ADMIN')")
public class ServiceController extends AbstractWebController {

    private final ServiceUseCase serviceUseCase;
    private final ServiceWebMapper serviceWebMapper;

    public ServiceController(ServiceUseCase serviceUseCase, ServiceWebMapper serviceWebMapper) {
        this.serviceUseCase = serviceUseCase;
        this.serviceWebMapper = serviceWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(@Valid @RequestBody CreateServiceRequest request) {
        return serviceWebMapper.toResponse(serviceUseCase.createService(serviceWebMapper.toDomain(request)));
    }

    @PutMapping("/{id}")
    public ServiceResponse updateService(@PathVariable Long id,
                                         @Valid @RequestBody UpdateServiceRequest request) {
        Service serviceToUpdate = new Service();
        serviceWebMapper.updateDomain(serviceToUpdate, request);
        return serviceWebMapper.toResponse(serviceUseCase.updateService(id, serviceToUpdate));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable Long id) {
        serviceUseCase.deleteService(id);
    }

    @GetMapping
    public List<ServiceResponse> getAllServices() {
        return mapResponses(serviceUseCase.getAllServices(), serviceWebMapper::toResponse);
    }

    @GetMapping("/allowed-for-track")
    public List<ServiceResponse> getAllServicesAllowedForTrack() {
        return mapResponses(serviceUseCase.getAllServicesAllowedForTrack(), serviceWebMapper::toResponse);
    }

    @GetMapping("/allowed-for-organizer")
    public List<ServiceResponse> getAllServicesAllowedForOrganizer() {
        return mapResponses(serviceUseCase.getAllServicesAllowedForOrganizer(), serviceWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ServiceResponse getServiceById(@PathVariable Long id) {
        return serviceWebMapper.toResponse(serviceUseCase.getServiceById(id));
    }

    @PatchMapping("/{id}/enable")
    public ServiceResponse enableService(@PathVariable Long id) {
        return serviceWebMapper.toResponse(serviceUseCase.enableService(id));
    }

    @PatchMapping("/{id}/disable")
    public ServiceResponse disableService(@PathVariable Long id) {
        return serviceWebMapper.toResponse(serviceUseCase.disableService(id));
    }
}
