package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerServiceUseCase;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerServiceWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizer-services")
public class OrganizerServiceController {

    private final OrganizerServiceUseCase organizerServiceUseCase;
    private final OrganizerServiceWebMapper organizerServiceWebMapper;

    public OrganizerServiceController(OrganizerServiceUseCase organizerServiceUseCase,
                                      OrganizerServiceWebMapper organizerServiceWebMapper) {
        this.organizerServiceUseCase = organizerServiceUseCase;
        this.organizerServiceWebMapper = organizerServiceWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizerServiceResponse createOrganizerService(@Valid @RequestBody CreateOrganizerServiceRequest request) {
        OrganizerService createdOrganizerService = organizerServiceUseCase.createOrganizerService(
                organizerServiceWebMapper.toDomain(request)
        );
        return organizerServiceWebMapper.toResponse(createdOrganizerService);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganizerService(@PathVariable Long id) {
        organizerServiceUseCase.deleteOrganizerService(id);
    }

    @GetMapping
    public List<OrganizerServiceResponse> getAllOrganizerServices() {
        return organizerServiceUseCase.getAllOrganizerServices()
                .stream()
                .map(organizerServiceWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public OrganizerServiceResponse getOrganizerServiceById(@PathVariable Long id) {
        return organizerServiceWebMapper.toResponse(organizerServiceUseCase.getOrganizerServiceById(id));
    }

    @GetMapping("/organizer/{organizerId}")
    public List<OrganizerServiceResponse> getOrganizerServicesByOrganizerId(@PathVariable Long organizerId) {
        return organizerServiceUseCase.getOrganizerServicesByOrganizerId(organizerId)
                .stream()
                .map(organizerServiceWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/service/{serviceId}")
    public List<OrganizerServiceResponse> getOrganizerServicesByServiceId(@PathVariable Long serviceId) {
        return organizerServiceUseCase.getOrganizerServicesByServiceId(serviceId)
                .stream()
                .map(organizerServiceWebMapper::toResponse)
                .toList();
    }
}
