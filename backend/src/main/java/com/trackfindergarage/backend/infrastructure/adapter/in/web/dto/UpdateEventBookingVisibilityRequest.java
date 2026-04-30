package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para cambiar la visibilidad pública de una reserva.
 *
 * <p>Se usa cuando el usuario decide si una reserva concreta debe mostrarse o no en su perfil
 * público.</p>
 */
@Getter
@Setter
public class UpdateEventBookingVisibilityRequest {

    @NotNull
    @JsonProperty("isVisible")
    private Boolean visible;
}
