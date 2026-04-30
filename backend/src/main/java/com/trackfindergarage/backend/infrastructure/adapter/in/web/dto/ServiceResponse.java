package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de salida con la información visible de un servicio del catálogo.
 *
 * <p>Incluye sus atributos funcionales y el estado de activación para que el frontend pueda
 * mostrarlo o filtrarlo correctamente.</p>
 */
@Getter
@Builder
public class ServiceResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean allowedForTrack;
    private Boolean allowedForOrganizer;
    private Boolean enabled;
}
