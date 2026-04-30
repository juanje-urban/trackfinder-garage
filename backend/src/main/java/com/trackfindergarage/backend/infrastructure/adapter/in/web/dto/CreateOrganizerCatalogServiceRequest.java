package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para habilitar un servicio del catálogo dentro del workspace del organizador.
 *
 * <p>Identifica el servicio general que el organizador quiere incorporar a su oferta propia.</p>
 */
@Getter
@Setter
public class CreateOrganizerCatalogServiceRequest {

    @NotNull
    private Long serviceId;
}
