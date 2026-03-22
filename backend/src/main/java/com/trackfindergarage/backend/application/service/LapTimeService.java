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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + extractUserId(lapTime)));

        Track track = trackPersistencePort.findById(extractTrackId(lapTime))
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + extractTrackId(lapTime)));

        lapTime.setUser(user);
        lapTime.setTrack(track);

        return lapTimePersistencePort.save(lapTime);
    }

    @Override
    public LapTime updateLapTime(Long id, LapTime lapTime) {
        validateLapTime(lapTime);

        LapTime existingLapTime = findLapTimeOrThrow(id);

        User user = userPersistencePort.findById(extractUserId(lapTime))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + extractUserId(lapTime)));

        Track track = trackPersistencePort.findById(extractTrackId(lapTime))
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + extractTrackId(lapTime)));

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
                .orElseThrow(() -> new ResourceNotFoundException("Lap time not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByUserId(Long userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return lapTimePersistencePort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByTrackId(Long trackId) {
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        return lapTimePersistencePort.findByTrackId(trackId);
    }

    @Override
    @Transactional(readOnly = true)
    public LapTime getBestLapTimeByTrackId(Long trackId) {
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        return lapTimePersistencePort.findByTrackId(trackId)
                .stream()
                .min(lapTimeComparator())
                .orElseThrow(() -> new ResourceNotFoundException("No lap times found for track id: " + trackId));
    }

    @Override
    @Transactional(readOnly = true)
    public LapTime getBestLapTimeByUserIdAndTrackId(Long userId, Long trackId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        return lapTimePersistencePort.findByUserIdAndTrackId(userId, trackId)
                .stream()
                .min(lapTimeComparator())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No lap times found for user id " + userId + " and track id " + trackId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getRankingByTrackId(Long trackId) {
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        return lapTimePersistencePort.findByTrackId(trackId)
                .stream()
                .sorted(lapTimeComparator())
                .toList();
    }

    private void validateLapTime(LapTime lapTime) {
        if (lapTime.getUser() == null || lapTime.getUser().getId() == null) {
            throw new IllegalArgumentException("User id is required");
        }
        if (lapTime.getTrack() == null || lapTime.getTrack().getId() == null) {
            throw new IllegalArgumentException("Track id is required");
        }
        if (lapTime.getLapDate() == null) {
            throw new IllegalArgumentException("Lap date is required");
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
                .orElseThrow(() -> new ResourceNotFoundException("Lap time not found with id: " + id));
    }
}
