package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizers")
@PreAuthorize("hasRole('ADMIN')")
public class OrganizerController extends AbstractWebController {

    private final OrganizerUseCase organizerUseCase;
    private final OrganizerWebMapper organizerWebMapper;

    public OrganizerController(OrganizerUseCase organizerUseCase, OrganizerWebMapper organizerWebMapper) {
        this.organizerUseCase = organizerUseCase;
        this.organizerWebMapper = organizerWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizerResponse createOrganizer(@Valid @RequestBody CreateOrganizerRequest request) {
        Organizer organizerToCreate = organizerWebMapper.toDomain(request);
        return organizerWebMapper.toResponse(organizerUseCase.createOrganizer(organizerToCreate, request.getPassword()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganizer(@PathVariable Long id) {
        organizerUseCase.deleteOrganizer(id);
    }

    @GetMapping
    public List<OrganizerResponse> getAllOrganizers() {
        return mapResponses(organizerUseCase.getAllOrganizers(), organizerWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    public OrganizerResponse getOrganizerById(@PathVariable Long id) {
        return organizerWebMapper.toResponse(organizerUseCase.getOrganizerById(id));
    }

    @PatchMapping("/{id}/enable")
    public OrganizerResponse enableOrganizer(@PathVariable Long id) {
        return organizerWebMapper.toResponse(organizerUseCase.enableOrganizer(id));
    }

    @PatchMapping("/{id}/disable")
    public OrganizerResponse disableOrganizer(@PathVariable Long id) {
        return organizerWebMapper.toResponse(organizerUseCase.disableOrganizer(id));
    }
}
