package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para asociar un servicio del catálogo a un circuito.
 *
 * <p>Modela la creación de una relación {@code TrackService} identificando únicamente el circuito y
 * el servicio involucrados.</p>
 */
@Getter
@Setter
public class CreateTrackServiceRequest {

    @NotNull
    private Long trackId;

    @NotNull
    private Long serviceId;
}
