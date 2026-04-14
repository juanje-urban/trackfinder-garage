package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.application.port.in.UpdateCurrentOrganizerProfileCommand;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateCurrentOrganizerProfileRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organizers")
public class CurrentOrganizerController extends AbstractWebController {

    private final OrganizerUseCase organizerUseCase;
    private final OrganizerWebMapper organizerWebMapper;

    public CurrentOrganizerController(OrganizerUseCase organizerUseCase,
                                      OrganizerWebMapper organizerWebMapper) {
        this.organizerUseCase = organizerUseCase;
        this.organizerWebMapper = organizerWebMapper;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ORGANIZER')")
    public OrganizerResponse getCurrentOrganizer(Authentication authentication) {
        return organizerWebMapper.toResponse(organizerUseCase.getCurrentOrganizer(authenticatedEmail(authentication)));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ORGANIZER')")
    public OrganizerResponse updateCurrentOrganizer(Authentication authentication,
                                                    @Valid @RequestBody UpdateCurrentOrganizerProfileRequest request) {
        return organizerWebMapper.toResponse(
                organizerUseCase.updateCurrentOrganizerProfile(
                        authenticatedEmail(authentication),
                        new UpdateCurrentOrganizerProfileCommand(
                                request.getName(),
                                request.getSurname(),
                                request.getEmail(),
                                request.getAddress(),
                                request.getPhone(),
                                request.getLegalName(),
                                request.getCif(),
                                request.getPassword()
                        )
                )
        );
    }
}
