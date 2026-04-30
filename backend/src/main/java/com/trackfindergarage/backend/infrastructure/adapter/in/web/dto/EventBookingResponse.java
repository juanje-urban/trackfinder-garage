package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida con la información principal de una reserva de evento.
 *
 * <p>Resume el usuario, el evento reservado, el precio base pagado y la preferencia de visibilidad
 * pública asociada a esa inscripción.</p>
 */
@Getter
@Builder
public class EventBookingResponse {

    private Long id;
    private Long userId;
    private String userDisplayName;
    private Long eventId;
    private LocalDate eventDate;
    private String organizerLegalName;
    private String trackName;
    private LocalDateTime bookedAt;
    private BigDecimal basePriceAtPurchase;

    @JsonProperty("isVisible")
    private boolean visible;
}
