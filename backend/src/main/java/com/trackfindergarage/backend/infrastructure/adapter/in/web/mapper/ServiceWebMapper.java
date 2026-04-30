package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.ServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateServiceRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper web para convertir servicios entre DTOs HTTP y modelo de dominio.
 *
 * <p>Traduce peticiones de creación o edición al agregado {@link Service} y genera respuestas listas
 * para el frontend a partir del dominio.</p>
 */
@Component
public class ServiceWebMapper {

    public Service toDomain(CreateServiceRequest request) {
        Service service = new Service();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setAllowedForTrack(request.getAllowedForTrack());
        service.setAllowedForOrganizer(request.getAllowedForOrganizer());
        return service;
    }

    public void updateDomain(Service service, UpdateServiceRequest request) {
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setAllowedForTrack(request.getAllowedForTrack());
        service.setAllowedForOrganizer(request.getAllowedForOrganizer());
    }

    public ServiceResponse toResponse(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .allowedForTrack(service.getAllowedForTrack())
                .allowedForOrganizer(service.getAllowedForOrganizer())
                .enabled(service.getEnabled())
                .build();
    }
}
