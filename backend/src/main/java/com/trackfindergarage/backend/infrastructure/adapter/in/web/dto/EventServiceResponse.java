package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO de salida con un servicio ofertado dentro de un evento.
 *
 * <p>Representa tanto extras heredados del circuito como servicios propios del organizador,
 * incluyendo su precio y si ya están contratados en alguna reserva.</p>
 */
@Getter
@Builder
public class EventServiceResponse {

    private Long id;
    private Long eventId;
    private Long trackServiceId;
    private String trackServiceName;
    private Long organizerServiceId;
    private String organizerServiceName;
    private BigDecimal price;
    private Boolean hasBookings;
}
