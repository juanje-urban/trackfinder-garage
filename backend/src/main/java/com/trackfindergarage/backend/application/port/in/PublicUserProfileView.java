package com.trackfindergarage.backend.application.port.in;

public record PublicUserProfileView(
        Long id,
        String displayName,
        long completedEvents,
        long visitedCircuits,
        long topFiveLapTimes,
        long poleCount
) {
}
