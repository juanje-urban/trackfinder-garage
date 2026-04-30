package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida con la representación pública de un evento.
 *
 * <p>Expone la información necesaria para listar o detallar un track day, incluyendo circuito,
 * organizador, precio base y capacidad restante.</p>
 */
@Getter
@Builder
public class EventResponse {

    private Long id;
    private Long organizerId;
    private String organizerLegalName;
    private Long trackId;
    private String trackName;
    private String trackShortName;
    private LocalDate eventDate;
    private BigDecimal basePrice;
    private int maxParticipants;
    private int remainingCapacity;
    private String description;
}
