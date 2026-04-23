package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.LapTime;

import java.util.List;

public interface LapTimeUseCase {

    LapTime createLapTimeForAuthenticatedUser(String authenticatedEmail,
                                              Long trackId,
                                              java.time.LocalDate lapDate,
                                              Long lapTimeMs,
                                              String vehicle);

    void deleteOwnLapTime(String authenticatedEmail, Long id);

    List<LapTime> getLapTimesByAuthenticatedEmail(String authenticatedEmail);

    List<LapTime> getLapTimesByUserId(Long userId);

    List<LapTime> getRankingByTrackId(Long trackId);
}
