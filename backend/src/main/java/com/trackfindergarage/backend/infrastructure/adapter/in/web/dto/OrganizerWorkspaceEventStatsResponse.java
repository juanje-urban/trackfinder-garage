package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class OrganizerWorkspaceEventStatsResponse {

    private Long eventId;
    private String trackName;
    private LocalDate eventDate;
    private long bookings;
    private long soldServices;
    private int remainingCapacity;
    private int totalCapacity;
    private BigDecimal baseRevenue;
    private BigDecimal serviceRevenue;
    private BigDecimal grossRevenue;
}
