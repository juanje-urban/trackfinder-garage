package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de entrada que representa un servicio incluido en el alta o edición de un evento.
 *
 * <p>Permite referenciar tanto servicios heredados del circuito como servicios propios del
 * organizador, junto con el precio concreto que tendrán en ese evento.</p>
 */
@Getter
@Setter
public class OrganizerEventServiceInput {

    private Long trackServiceId;

    private Long organizerServiceId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}
