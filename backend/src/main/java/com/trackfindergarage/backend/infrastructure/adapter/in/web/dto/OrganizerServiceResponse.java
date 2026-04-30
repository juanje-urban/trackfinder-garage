package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de salida con un servicio habilitado por un organizador.
 *
 * <p>Expone la relación entre organizador y servicio del catálogo, incluyendo si esa relación está
 * activa para poder ofrecerse en eventos.</p>
 */
@Getter
@Builder
public class OrganizerServiceResponse {

    private Long id;
    private Long organizerId;
    private String organizerLegalName;
    private Long serviceId;
    private String serviceName;
    private Boolean enabled;
}
