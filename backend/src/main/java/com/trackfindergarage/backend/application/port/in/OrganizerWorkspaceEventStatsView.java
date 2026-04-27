package com.trackfindergarage.backend.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Estadísticas de un evento concreto.
 */
public record OrganizerWorkspaceEventStatsView(
        Long eventId,
        String trackName,
        LocalDate eventDate,
        long bookings,
        long soldServices,
        int remainingCapacity,
        int totalCapacity,
        BigDecimal baseRevenue,
        BigDecimal serviceRevenue,
        BigDecimal grossRevenue
) {
}
