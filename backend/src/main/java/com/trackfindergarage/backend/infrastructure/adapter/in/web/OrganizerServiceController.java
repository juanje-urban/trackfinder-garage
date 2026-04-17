package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerServiceUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerServiceWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizer-services")
public class OrganizerServiceController extends AbstractWebController {

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
        return organizerServiceWebMapper.toResponse(
                organizerServiceUseCase.createOrganizerService(organizerServiceWebMapper.toDomain(request))
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganizerService(@PathVariable Long id) {
        organizerServiceUseCase.deleteOrganizerService(id);
    }

    @GetMapping
    public List<OrganizerServiceResponse> getAllOrganizerServices() {
        return mapResponses(organizerServiceUseCase.getAllOrganizerServices(), organizerServiceWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    public OrganizerServiceResponse getOrganizerServiceById(@PathVariable Long id) {
        return organizerServiceWebMapper.toResponse(organizerServiceUseCase.getOrganizerServiceById(id));
    }

    @GetMapping("/organizer/{organizerId}")
    public List<OrganizerServiceResponse> getOrganizerServicesByOrganizerId(@PathVariable Long organizerId) {
        return mapResponses(
                organizerServiceUseCase.getOrganizerServicesByOrganizerId(organizerId),
                organizerServiceWebMapper::toResponse
        );
    }

    @GetMapping("/service/{serviceId}")
    public List<OrganizerServiceResponse> getOrganizerServicesByServiceId(@PathVariable Long serviceId) {
        return mapResponses(
                organizerServiceUseCase.getOrganizerServicesByServiceId(serviceId),
                organizerServiceWebMapper::toResponse
        );
    }
}
