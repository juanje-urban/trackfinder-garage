package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerServiceResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper web para convertir servicios de organizador en respuestas HTTP.
 *
 * <p>Adapta la relación entre organizador y servicio del catálogo a una vista plana lista para el
 * frontend del workspace.</p>
 */
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
