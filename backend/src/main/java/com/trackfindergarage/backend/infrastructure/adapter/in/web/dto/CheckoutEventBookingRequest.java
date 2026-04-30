package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de entrada para confirmar la contratación de una reserva de evento.
 *
 * <p>Incluye el evento elegido, los servicios extra seleccionados y la preferencia de visibilidad
 * pública de la reserva en el perfil del usuario y en la lista de asistentes al evento.</p>
 */
@Getter
@Setter
public class CheckoutEventBookingRequest {

    @NotNull
    private Long eventId;

    private List<Long> eventServiceIds = new ArrayList<>();

    @NotNull
    @JsonProperty("isVisible")
    private Boolean visible;
}
