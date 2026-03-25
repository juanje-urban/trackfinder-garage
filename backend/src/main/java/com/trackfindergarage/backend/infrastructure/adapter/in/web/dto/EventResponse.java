package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EventResponse {

    private Long id;
    private Long organizerId;
    private String organizerLegalName;
    private Long trackId;
    private String trackName;
    private LocalDate eventDate;
    private BigDecimal basePrice;
    private Integer maxParticipants;
    private Integer remainingCapacity;
}
