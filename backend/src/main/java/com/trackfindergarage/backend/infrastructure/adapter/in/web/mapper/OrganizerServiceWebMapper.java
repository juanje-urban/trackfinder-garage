package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class OrganizerServiceWebMapper {

    public OrganizerServiceResponse toResponse(OrganizerService organizerService) {
        return OrganizerServiceResponse.builder()
                .id(organizerService.getId())
                .organizerId(
                        organizerService.getOrganizer() != null ? organizerService.getOrganizer().getIdUser() : null
                )
                .organizerLegalName(
                        organizerService.getOrganizer() != null ? organizerService.getOrganizer().getLegalName() : null
                )
                .serviceId(organizerService.getService() != null ? organizerService.getService().getId() : null)
                .serviceName(
                        organizerService.getService() != null ? organizerService.getService().getName() : null
                )
                .enabled(organizerService.getEnabled())
                .build();
    }
}
