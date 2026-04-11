package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.LapTime;

import java.util.List;

public interface LapTimeUseCase {

    LapTime createLapTime(LapTime lapTime);

    LapTime createLapTimeForAuthenticatedUser(String authenticatedEmail,
                                              Long trackId,
                                              java.time.LocalDate lapDate,
                                              Long lapTimeMs,
                                              String vehicle);

    LapTime updateLapTime(Long id, LapTime lapTime);

    void deleteOwnLapTime(String authenticatedEmail, Long id);

    void deleteLapTime(Long id);

    List<LapTime> getAllLapTimes();

    LapTime getLapTimeById(Long id);

    List<LapTime> getLapTimesByAuthenticatedEmail(String authenticatedEmail);

    List<LapTime> getLapTimesByUserId(Long userId);

    List<LapTime> getLapTimesByTrackId(Long trackId);

    LapTime getBestLapTimeByTrackId(Long trackId);

    LapTime getBestLapTimeByUserIdAndTrackId(Long userId, Long trackId);

    List<LapTime> getRankingByTrackId(Long trackId);
}
