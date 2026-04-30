package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de salida con la representación pública de un circuito.
 *
 * <p>Expone la información descriptiva mínima necesaria para listados, detalle y selección de
 * circuitos en el frontend.</p>
 */
@Getter
@Builder
public class TrackResponse {

    private Long id;
    private String name;
    private String shortName;
    private String location;
    private String description;
}
