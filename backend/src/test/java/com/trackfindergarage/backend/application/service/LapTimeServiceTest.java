package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.LapTimePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LapTimeServiceTest {

    @Mock
    private LapTimePersistencePort lapTimePersistencePort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private TrackPersistencePort trackPersistencePort;

    @InjectMocks
    private LapTimeService lapTimeService;

    @Test
    void createLapTimePersistsWhenDataIsValid() {
        LapTime lapTime = lapTimeWithIds(1L, 2L, 91000L, LocalDate.now());
        User user = userWithId(1L);
        Track track = trackWithId(2L);

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(trackPersistencePort.findById(2L)).thenReturn(Optional.of(track));
        when(lapTimePersistencePort.save(lapTime)).thenReturn(lapTime);

        LapTime created = lapTimeService.createLapTime(lapTime);

        assertSame(lapTime, created);
        assertSame(user, lapTime.getUser());
        assertSame(track, lapTime.getTrack());
        verify(lapTimePersistencePort).save(lapTime);
    }

    @Test
    void createLapTimeThrowsWhenDateIsInFuture() {
        LapTime lapTime = lapTimeWithIds(1L, 2L, 91000L, LocalDate.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> lapTimeService.createLapTime(lapTime));
    }

    @Test
    void updateLapTimeCopiesMutableFieldsAndSaves() {
        LapTime existingLapTime = lapTimeWithIds(1L, 2L, 95000L, LocalDate.now().minusDays(2));
        existingLapTime.setId(5L);

        LapTime updateRequest = lapTimeWithIds(3L, 4L, 90000L, LocalDate.now().minusDays(1));
        updateRequest.setVehicle("Car B");

        User user = userWithId(3L);
        Track track = trackWithId(4L);

        when(lapTimePersistencePort.findById(5L)).thenReturn(Optional.of(existingLapTime));
        when(userPersistencePort.findById(3L)).thenReturn(Optional.of(user));
        when(trackPersistencePort.findById(4L)).thenReturn(Optional.of(track));
        when(lapTimePersistencePort.save(existingLapTime)).thenReturn(existingLapTime);

        LapTime updated = lapTimeService.updateLapTime(5L, updateRequest);

        assertSame(existingLapTime, updated);
        assertSame(user, existingLapTime.getUser());
        assertSame(track, existingLapTime.getTrack());
        assertEquals(90000L, existingLapTime.getLapTimeMs());
        assertEquals("Car B", existingLapTime.getVehicle());
    }

    @Test
    void getBestLapTimeByTrackIdReturnsFastestLap() {
        LapTime slow = lapTimeWithIds(1L, 2L, 95000L, LocalDate.now().minusDays(2));
        slow.setId(1L);
        LapTime fast = lapTimeWithIds(2L, 2L, 90000L, LocalDate.now().minusDays(1));
        fast.setId(2L);

        when(trackPersistencePort.findById(2L)).thenReturn(Optional.of(trackWithId(2L)));
        when(lapTimePersistencePort.findByTrackId(2L)).thenReturn(List.of(slow, fast));

        LapTime best = lapTimeService.getBestLapTimeByTrackId(2L);

        assertSame(fast, best);
    }

    @Test
    void getBestLapTimeByUserIdAndTrackIdReturnsFastestLapForUserOnTrack() {
        LapTime slow = lapTimeWithIds(1L, 2L, 95000L, LocalDate.now().minusDays(2));
        LapTime fast = lapTimeWithIds(1L, 2L, 90000L, LocalDate.now().minusDays(1));

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(trackPersistencePort.findById(2L)).thenReturn(Optional.of(trackWithId(2L)));
        when(lapTimePersistencePort.findByUserIdAndTrackId(1L, 2L)).thenReturn(List.of(slow, fast));

        LapTime best = lapTimeService.getBestLapTimeByUserIdAndTrackId(1L, 2L);

        assertSame(fast, best);
    }

    @Test
    void getRankingByTrackIdReturnsAllLapTimesSortedByAbsoluteTime() {
        LapTime userOneSlow = lapTimeWithIds(1L, 2L, 95000L, LocalDate.now().minusDays(3));
        userOneSlow.setId(1L);
        LapTime userOneFast = lapTimeWithIds(1L, 2L, 92000L, LocalDate.now().minusDays(2));
        userOneFast.setId(2L);
        LapTime userTwoFast = lapTimeWithIds(2L, 2L, 90000L, LocalDate.now().minusDays(1));
        userTwoFast.setId(3L);

        when(trackPersistencePort.findById(2L)).thenReturn(Optional.of(trackWithId(2L)));
        when(lapTimePersistencePort.findByTrackId(2L)).thenReturn(List.of(userOneSlow, userOneFast, userTwoFast));

        List<LapTime> ranking = lapTimeService.getRankingByTrackId(2L);

        assertEquals(3, ranking.size());
        assertSame(userTwoFast, ranking.get(0));
        assertSame(userOneFast, ranking.get(1));
        assertSame(userOneSlow, ranking.get(2));
    }

    @Test
    void getLapTimeByIdThrowsWhenLapTimeDoesNotExist() {
        when(lapTimePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lapTimeService.getLapTimeById(99L));
    }

    private LapTime lapTimeWithIds(Long userId, Long trackId, Long lapTimeMs, LocalDate lapDate) {
        User user = userWithId(userId);
        Track track = trackWithId(trackId);

        LapTime lapTime = new LapTime();
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setLapDate(lapDate);
        lapTime.setVehicle("Car A");
        return lapTime;
    }

    private User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        user.setDisplayName("user-" + id);
        return user;
    }

    private Track trackWithId(Long id) {
        Track track = new Track();
        track.setId(id);
        track.setName("track-" + id);
        return track;
    }
}
