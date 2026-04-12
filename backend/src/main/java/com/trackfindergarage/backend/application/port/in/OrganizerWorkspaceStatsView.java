package com.trackfindergarage.backend.application.port.in;

import java.math.BigDecimal;
import java.util.List;

public record OrganizerWorkspaceStatsView(
        BigDecimal totalBaseRevenue,
        BigDecimal totalServiceRevenue,
        BigDecimal totalGrossRevenue,
        long totalBookings,
        long totalSoldServices,
        long futureEvents,
        long pastEvents,
        int totalCapacity,
        int totalRemainingCapacity,
        List<OrganizerWorkspaceEventStatsView> eventStats
) {
}
