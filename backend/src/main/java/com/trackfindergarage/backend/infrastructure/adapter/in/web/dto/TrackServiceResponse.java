package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de salida con una asociación entre circuito y servicio.
 *
 * <p>Expone tanto los identificadores como los nombres legibles de cada extremo de la relación para
 * facilitar su uso en tablas y formularios del frontend.</p>
 */
@Getter
@Builder
public class TrackServiceResponse {

    private Long id;
    private Long trackId;
    private String trackName;
    private Long serviceId;
    private String serviceName;
}
