package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para actualizar un servicio existente del catálogo.
 *
 * <p>Permite modificar tanto sus datos descriptivos como las banderas que indican dónde puede
 * utilizarse (circuito, organizador o ambos).</p>
 */
@Getter
@Setter
public class UpdateServiceRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    private Boolean allowedForTrack;

    @NotNull
    private Boolean allowedForOrganizer;
}
