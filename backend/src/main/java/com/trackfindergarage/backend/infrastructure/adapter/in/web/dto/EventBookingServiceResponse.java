package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO de salida con un servicio extra contratado dentro de una reserva.
 *
 * <p>Permite relacionar una línea de detalle con la reserva, el evento y el servicio concreto, así
 * como mostrar el precio capturado en el momento de la compra.</p>
 */
@Getter
@Builder
public class EventBookingServiceResponse {

    private Long id;
    private Long eventBookingId;
    private Long userId;
    private Long eventId;
    private Long eventServiceId;
    private BigDecimal priceAtPurchase;
}
