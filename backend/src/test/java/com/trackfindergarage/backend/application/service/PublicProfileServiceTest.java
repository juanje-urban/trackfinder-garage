package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.PublicUserProfileView;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.LapTimePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicProfileServiceTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private EventBookingPersistencePort eventBookingPersistencePort;

    @Mock
    private LapTimePersistencePort lapTimePersistencePort;

    @InjectMocks
    private PublicProfileService publicProfileService;

    @Test
    void getPublicUserProfileAggregatesPublicMetrics() {
        User user = user(1L, "driver");

        EventBooking pastJaramaBooking = booking(10L, event(100L, 7L, LocalDate.now().minusDays(30)));
        EventBooking pastMontmeloBooking = booking(11L, event(101L, 8L, LocalDate.now().minusDays(10)));
        EventBooking futureBooking = booking(12L, event(102L, 9L, LocalDate.now().plusDays(10)));

        LapTime userPole = lapTime(1L, user, track(7L, "Jarama"), 90000L, LocalDate.of(2026, 1, 10));
        LapTime userTopFive = lapTime(2L, user, track(8L, "Montmelo"), 98000L, LocalDate.of(2026, 1, 11));

        when(userPersistencePort.findByDisplayName("driver")).thenReturn(Optional.of(user));
        when(eventBookingPersistencePort.findByUserId(1L))
                .thenReturn(List.of(pastJaramaBooking, pastMontmeloBooking, futureBooking));
        when(lapTimePersistencePort.findByUserId(1L)).thenReturn(List.of(userPole, userTopFive));
        when(lapTimePersistencePort.findByTrackId(7L))
                .thenReturn(List.of(
                        userPole,
                        lapTime(3L, user(2L, "other"), track(7L, "Jarama"), 90500L, LocalDate.of(2026, 1, 12))
                ));
        when(lapTimePersistencePort.findByTrackId(8L))
                .thenReturn(List.of(
                        lapTime(4L, user(2L, "other"), track(8L, "Montmelo"), 93000L, LocalDate.of(2026, 1, 10)),
                        lapTime(5L, user(3L, "other2"), track(8L, "Montmelo"), 94000L, LocalDate.of(2026, 1, 10)),
                        lapTime(6L, user(4L, "other3"), track(8L, "Montmelo"), 95000L, LocalDate.of(2026, 1, 10)),
                        lapTime(7L, user(5L, "other4"), track(8L, "Montmelo"), 96000L, LocalDate.of(2026, 1, 10)),
                        userTopFive
                ));

        PublicUserProfileView profile = publicProfileService.getPublicUserProfile(" driver ");

        assertEquals(1L, profile.id());
        assertEquals("driver", profile.displayName());
        assertEquals(2L, profile.completedEvents());
        assertEquals(2L, profile.visitedCircuits());
        assertEquals(2L, profile.topFiveLapTimes());
        assertEquals(1L, profile.poleCount());
    }

    @Test
    void getPublicUserProfileThrowsWhenDisplayNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> publicProfileService.getPublicUserProfile(" "));
    }

    @Test
    void getPublicUserProfileThrowsWhenUserDoesNotExist() {
        when(userPersistencePort.findByDisplayName("driver")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publicProfileService.getPublicUserProfile("driver"));
    }

    private User user(Long id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        return user;
    }

    private Track track(Long id, String name) {
        Track track = new Track();
        track.setId(id);
        track.setName(name);
        return track;
    }

    private Event event(Long id, Long trackId, LocalDate eventDate) {
        Event event = new Event();
        event.setId(id);
        event.setTrack(track(trackId, "Track-" + trackId));
        event.setEventDate(eventDate);
        return event;
    }

    private EventBooking booking(Long id, Event event) {
        EventBooking booking = new EventBooking();
        booking.setId(id);
        booking.setEvent(event);
        return booking;
    }

    private LapTime lapTime(Long id, User user, Track track, Long lapTimeMs, LocalDate date) {
        LapTime lapTime = new LapTime();
        lapTime.setId(id);
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setLapDate(date);
        return lapTime;
    }
}
