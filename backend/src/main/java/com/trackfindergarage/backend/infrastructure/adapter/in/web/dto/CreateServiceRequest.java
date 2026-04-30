package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para crear un servicio del catálogo general.
 *
 * <p>Define la información básica del servicio y en qué contextos puede ofrecerse dentro de la
 * aplicación (disponible para organizadores, circuitos o ambos).</p>
 */
@Getter
@Setter
public class CreateServiceRequest {

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
