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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    void createLapTimeForAuthenticatedUserAttachesLoadedReferencesAndSaves() {
        User user = user(1L, "driver");
        Track track = track(7L, "Jarama");

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(trackPersistencePort.findById(7L)).thenReturn(Optional.of(track));
        when(lapTimePersistencePort.save(any(LapTime.class))).then(returnsFirstArg());

        LapTime createdLapTime = lapTimeService.createLapTimeForAuthenticatedUser(
                " DRIVER@example.com ",
                7L,
                LocalDate.now().minusDays(1),
                91234L,
                "BMW M4"
        );

        assertSame(user, createdLapTime.getUser());
        assertSame(track, createdLapTime.getTrack());
        assertEquals(91234L, createdLapTime.getLapTimeMs());
        assertEquals("BMW M4", createdLapTime.getVehicle());
        verify(lapTimePersistencePort).save(createdLapTime);
    }

    @Test
    void createLapTimeForAuthenticatedUserRejectsFutureDates() {
        User user = user(1L, "driver");
        LocalDate futureLapDate = LocalDate.now().plusDays(1);
        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> lapTimeService.createLapTimeForAuthenticatedUser(
                        "driver@example.com",
                        7L,
                        futureLapDate,
                        91234L,
                        "BMW M4"
                )
        );
        verify(lapTimePersistencePort, never()).save(any(LapTime.class));
    }

    @Test
    void deleteOwnLapTimeDeletesOwnedLapTime() {
        User user = user(1L, "driver");
        LapTime lapTime = lapTime(5L, user, track(7L, "Jarama"), LocalDate.now().minusDays(2), 92000L);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(lapTimePersistencePort.findById(5L)).thenReturn(Optional.of(lapTime));

        lapTimeService.deleteOwnLapTime("driver@example.com", 5L);

        verify(lapTimePersistencePort).delete(lapTime);
    }

    @Test
    void deleteOwnLapTimeThrowsWhenUserIsNotOwner() {
        User authenticatedUser = user(1L, "driver");
        User owner = user(2L, "other");
        LapTime lapTime = lapTime(5L, owner, track(7L, "Jarama"), LocalDate.now().minusDays(2), 92000L);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(authenticatedUser));
        when(lapTimePersistencePort.findById(5L)).thenReturn(Optional.of(lapTime));

        assertThrows(
                AccessDeniedException.class,
                () -> lapTimeService.deleteOwnLapTime("driver@example.com", 5L)
        );
        verify(lapTimePersistencePort, never()).delete(lapTime);
    }

    @Test
    void getLapTimesByAuthenticatedEmailReturnsUserLapTimes() {
        User user = user(1L, "driver");
        List<LapTime> lapTimes = List.of(lapTime(5L, user, track(7L, "Jarama"), LocalDate.now().minusDays(2), 92000L));

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(lapTimePersistencePort.findByUserId(1L)).thenReturn(lapTimes);

        assertEquals(lapTimes, lapTimeService.getLapTimesByAuthenticatedEmail("driver@example.com"));
    }

    @Test
    void getLapTimesByUserIdThrowsWhenUserDoesNotExist() {
        when(userPersistencePort.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lapTimeService.getLapTimesByUserId(9L));
    }

    @Test
    void getRankingByTrackIdSortsByLapTimeDateAndId() {
        Track track = track(7L, "Jarama");
        User user = user(1L, "driver");

        LapTime slower = lapTime(3L, user, track, LocalDate.of(2026, 4, 10), 93000L);
        LapTime earlierTie = lapTime(2L, user, track, LocalDate.of(2026, 4, 8), 92000L);
        LapTime laterTie = lapTime(1L, user, track, LocalDate.of(2026, 4, 9), 92000L);

        when(trackPersistencePort.findById(7L)).thenReturn(Optional.of(track));
        when(lapTimePersistencePort.findByTrackId(7L)).thenReturn(List.of(slower, laterTie, earlierTie));

        List<LapTime> ranking = lapTimeService.getRankingByTrackId(7L);

        assertEquals(List.of(earlierTie, laterTie, slower), ranking);
    }

    @Test
    void getRankingByTrackIdThrowsWhenTrackDoesNotExist() {
        when(trackPersistencePort.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lapTimeService.getRankingByTrackId(7L));
    }

    private User user(Long id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName.toLowerCase() + "@example.com");
        return user;
    }

    private Track track(Long id, String name) {
        Track track = new Track();
        track.setId(id);
        track.setName(name);
        track.setShortName(name.toLowerCase());
        track.setLocation("Madrid");
        track.setDescription("Circuito");
        return track;
    }

    private LapTime lapTime(Long id, User user, Track track, LocalDate date, Long lapTimeMs) {
        LapTime lapTime = new LapTime();
        lapTime.setId(id);
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(date);
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setVehicle("BMW M4");
        return lapTime;
    }
}
