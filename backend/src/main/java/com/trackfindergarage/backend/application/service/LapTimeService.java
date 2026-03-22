package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.application.port.out.LapTimePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class LapTimeService implements LapTimeUseCase {

    private static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    private static final String TRACK_NOT_FOUND_WITH_ID = "Track not found with id: ";
    private static final String LAP_TIME_NOT_FOUND_WITH_ID = "Lap time not found with id: ";
    private static final String USER_ID_REQUIRED = "User id is required";
    private static final String TRACK_ID_REQUIRED = "Track id is required";
    private static final String LAP_DATE_REQUIRED = "Lap date is required";
    private static final String NO_LAP_TIMES_FOUND_FOR_TRACK_ID = "No lap times found for track id: ";
    private static final String NO_LAP_TIMES_FOUND_FOR_USER_AND_TRACK =
            "No lap times found for user id %d and track id %d";

    private final LapTimePersistencePort lapTimePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final TrackPersistencePort trackPersistencePort;

    public LapTimeService(LapTimePersistencePort lapTimePersistencePort,
                          UserPersistencePort userPersistencePort,
                          TrackPersistencePort trackPersistencePort) {
        this.lapTimePersistencePort = lapTimePersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.trackPersistencePort = trackPersistencePort;
    }

    @Override
    public LapTime createLapTime(LapTime lapTime) {
        validateLapTime(lapTime);

        User user = userPersistencePort.findById(extractUserId(lapTime))
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + extractUserId(lapTime)));

        Track track = trackPersistencePort.findById(extractTrackId(lapTime))
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + extractTrackId(lapTime)));

        lapTime.setUser(user);
        lapTime.setTrack(track);

        return lapTimePersistencePort.save(lapTime);
    }

    @Override
    public LapTime updateLapTime(Long id, LapTime lapTime) {
        validateLapTime(lapTime);

        LapTime existingLapTime = findLapTimeOrThrow(id);

        User user = userPersistencePort.findById(extractUserId(lapTime))
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + extractUserId(lapTime)));

        Track track = trackPersistencePort.findById(extractTrackId(lapTime))
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + extractTrackId(lapTime)));

        existingLapTime.setUser(user);
        existingLapTime.setTrack(track);
        existingLapTime.setLapDate(lapTime.getLapDate());
        existingLapTime.setLapTimeMs(lapTime.getLapTimeMs());
        existingLapTime.setVehicle(lapTime.getVehicle());

        return lapTimePersistencePort.save(existingLapTime);
    }

    @Override
    public void deleteLapTime(Long id) {
        LapTime lapTime = findLapTimeOrThrow(id);
        lapTimePersistencePort.delete(lapTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getAllLapTimes() {
        return lapTimePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public LapTime getLapTimeById(Long id) {
        return lapTimePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(LAP_TIME_NOT_FOUND_WITH_ID + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByUserId(Long userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));

        return lapTimePersistencePort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByTrackId(Long trackId) {
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        return lapTimePersistencePort.findByTrackId(trackId);
    }

    @Override
    @Transactional(readOnly = true)
    public LapTime getBestLapTimeByTrackId(Long trackId) {
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        return lapTimePersistencePort.findByTrackId(trackId)
                .stream()
                .min(lapTimeComparator())
                .orElseThrow(() -> new ResourceNotFoundException(NO_LAP_TIMES_FOUND_FOR_TRACK_ID + trackId));
    }

    @Override
    @Transactional(readOnly = true)
    public LapTime getBestLapTimeByUserIdAndTrackId(Long userId, Long trackId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        return lapTimePersistencePort.findByUserIdAndTrackId(userId, trackId)
                .stream()
                .min(lapTimeComparator())
                .orElseThrow(() -> new ResourceNotFoundException(
                        NO_LAP_TIMES_FOUND_FOR_USER_AND_TRACK.formatted(userId, trackId)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getRankingByTrackId(Long trackId) {
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        return lapTimePersistencePort.findByTrackId(trackId)
                .stream()
                .sorted(lapTimeComparator())
                .toList();
    }

    private void validateLapTime(LapTime lapTime) {
        if (lapTime.getUser() == null || lapTime.getUser().getId() == null) {
            throw new IllegalArgumentException(USER_ID_REQUIRED);
        }
        if (lapTime.getTrack() == null || lapTime.getTrack().getId() == null) {
            throw new IllegalArgumentException(TRACK_ID_REQUIRED);
        }
        if (lapTime.getLapDate() == null) {
            throw new IllegalArgumentException(LAP_DATE_REQUIRED);
        }
        if (lapTime.getLapDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Lap date cannot be in the future");
        }
        if (lapTime.getLapTimeMs() == null || lapTime.getLapTimeMs() <= 0) {
            throw new IllegalArgumentException("Lap time must be greater than 0");
        }
    }

    private Long extractUserId(LapTime lapTime) {
        return lapTime.getUser().getId();
    }

    private Long extractTrackId(LapTime lapTime) {
        return lapTime.getTrack().getId();
    }

    private Comparator<LapTime> lapTimeComparator() {
        return Comparator.comparing(LapTime::getLapTimeMs)
                .thenComparing(LapTime::getLapDate)
                .thenComparing(lapTime -> lapTime.getId() == null ? Long.MAX_VALUE : lapTime.getId());
    }

    private LapTime findLapTimeOrThrow(Long id) {
        return lapTimePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(LAP_TIME_NOT_FOUND_WITH_ID + id));
    }
}
