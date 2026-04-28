package com.trackfindergarage.backend.application.port.in;

/**
 * Resume las métricas públicas que se muestran en el perfil visible de un usuario.
 */
public record PublicUserProfileView(
        Long id,
        String displayName,
        long completedEvents,
        long visitedCircuits,
        long topFiveLapTimes,
        long poleCount
) {
}
