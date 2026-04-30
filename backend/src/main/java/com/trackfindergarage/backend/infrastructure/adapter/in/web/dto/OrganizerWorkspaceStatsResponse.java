package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de salida con las métricas globales del workspace del organizador.
 *
 * <p>Expone ingresos, ocupación y volumen de actividad tanto a nivel agregado como desglosado por
 * evento.</p>
 */
@Getter
@Builder
public class OrganizerWorkspaceStatsResponse {

    private BigDecimal totalBaseRevenue;
    private BigDecimal totalServiceRevenue;
    private BigDecimal totalGrossRevenue;
    private long totalBookings;
    private long totalSoldServices;
    private long futureEvents;
    private long pastEvents;
    private int totalCapacity;
    private int totalRemainingCapacity;
    private List<OrganizerWorkspaceEventStatsResponse> eventStats;
}
