package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWebMapper;
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

    @DeleteMapping("/{id}")
    public void deleteOrganizer(@PathVariable Long id) {
        organizerUseCase.deleteOrganizer(id);
    }

    @GetMapping
    public List<OrganizerResponse> getAllOrganizers() {
        return mapResponses(organizerUseCase.getAllOrganizers(), organizerWebMapper::toResponse);
    }

    @PatchMapping("/{id}/enable")
    public OrganizerResponse enableOrganizer(@PathVariable Long id) {
        return organizerWebMapper.toResponse(organizerUseCase.enableOrganizer(id));
    }
}
