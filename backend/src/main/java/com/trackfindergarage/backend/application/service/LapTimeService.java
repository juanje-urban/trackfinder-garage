package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.application.port.out.LapTimePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class LapTimeService implements LapTimeUseCase {

    private static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    private static final String TRACK_NOT_FOUND_WITH_ID = "Track not found with id: ";
    private static final String LAP_TIME_NOT_FOUND_WITH_ID = "Lap time not found with id: ";
    private static final String USER_ID_REQUIRED = "User id is required";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "Authenticated user email is required";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
    private static final String TRACK_ID_REQUIRED = "Track id is required";
    private static final String LAP_DATE_REQUIRED = "Lap date is required";
    private static final String NO_LAP_TIMES_FOUND_FOR_TRACK_ID = "No lap times found for track id: ";
    private static final String NO_LAP_TIMES_FOUND_FOR_USER_AND_TRACK =
            "No lap times found for user id %d and track id %d";
    private static final String ONLY_LAP_TIME_OWNER_CAN_DELETE = "Only the owner of the lap time can delete it";

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
        attachLapTimeReferences(lapTime, extractUserId(lapTime), extractTrackId(lapTime));
        return lapTimePersistencePort.save(lapTime);
    }

    @Override
    public LapTime createLapTimeForAuthenticatedUser(String authenticatedEmail,
                                                     Long trackId,
                                                     LocalDate lapDate,
                                                     Long lapTimeMs,
                                                     String vehicle) {
        User user = findUserByAuthenticatedEmail(authenticatedEmail);

        LapTime lapTime = new LapTime();
        lapTime.setUser(user);
        lapTime.setTrack(trackReference(trackId));
        lapTime.setLapDate(lapDate);
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setVehicle(vehicle);

        return createLapTime(lapTime);
    }

    @Override
    public LapTime updateLapTime(Long id, LapTime lapTime) {
        validateLapTime(lapTime);

        LapTime existingLapTime = findLapTimeOrThrow(id);
        attachLapTimeReferences(existingLapTime, extractUserId(lapTime), extractTrackId(lapTime));
        copyLapTimeValues(existingLapTime, lapTime);

        return lapTimePersistencePort.save(existingLapTime);
    }

    @Override
    public void deleteOwnLapTime(String authenticatedEmail, Long id) {
        User user = findUserByAuthenticatedEmail(authenticatedEmail);
        LapTime lapTime = findLapTimeOrThrow(id);

        if (lapTime.getUser() == null || !user.getId().equals(lapTime.getUser().getId())) {
            throw new AccessDeniedException(ONLY_LAP_TIME_OWNER_CAN_DELETE);
        }

        lapTimePersistencePort.delete(lapTime);
    }

    @Override
    public void deleteLapTime(Long id) {
        lapTimePersistencePort.delete(findLapTimeOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getAllLapTimes() {
        return lapTimePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public LapTime getLapTimeById(Long id) {
        return findLapTimeOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByAuthenticatedEmail(String authenticatedEmail) {
        return lapTimePersistencePort.findByUserId(findUserByAuthenticatedEmail(authenticatedEmail).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByUserId(Long userId) {
        requireUserExists(userId);
        return lapTimePersistencePort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByTrackId(Long trackId) {
        requireTrackExists(trackId);
        return lapTimePersistencePort.findByTrackId(trackId);
    }

    @Override
    @Transactional(readOnly = true)
    public LapTime getBestLapTimeByTrackId(Long trackId) {
        requireTrackExists(trackId);

        return lapTimePersistencePort.findByTrackId(trackId)
                .stream()
                .min(lapTimeComparator())
                .orElseThrow(() -> new ResourceNotFoundException(NO_LAP_TIMES_FOUND_FOR_TRACK_ID + trackId));
    }

    @Override
    @Transactional(readOnly = true)
    public LapTime getBestLapTimeByUserIdAndTrackId(Long userId, Long trackId) {
        requireUserExists(userId);
        requireTrackExists(trackId);

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
        requireTrackExists(trackId);

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

    private void attachLapTimeReferences(LapTime lapTime, Long userId, Long trackId) {
        lapTime.setUser(loadUserById(userId));
        lapTime.setTrack(loadTrackById(trackId));
    }

    private void copyLapTimeValues(LapTime target, LapTime source) {
        target.setLapDate(source.getLapDate());
        target.setLapTimeMs(source.getLapTimeMs());
        target.setVehicle(source.getVehicle());
    }

    private Track trackReference(Long trackId) {
        Track track = new Track();
        track.setId(trackId);
        return track;
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

    private User loadUserById(Long userId) {
        return userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));
    }

    private Track loadTrackById(Long trackId) {
        return trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));
    }

    private void requireUserExists(Long userId) {
        loadUserById(userId);
    }

    private void requireTrackExists(Long trackId) {
        loadTrackById(trackId);
    }

    private LapTime findLapTimeOrThrow(Long id) {
        return lapTimePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(LAP_TIME_NOT_FOUND_WITH_ID + id));
    }

    private User findUserByAuthenticatedEmail(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        return userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail));
    }
}
