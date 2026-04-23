package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.application.port.in.TrackUseCase;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackRecordResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackRecordWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackWebMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrackControllerTest {

    private final TrackUseCase trackUseCase = mock(TrackUseCase.class);
    private final LapTimeUseCase lapTimeUseCase = mock(LapTimeUseCase.class);
    private final TrackWebMapper trackWebMapper = new TrackWebMapper();
    private final TrackRecordWebMapper trackRecordWebMapper = new TrackRecordWebMapper();
    private final TrackController trackController =
            new TrackController(trackUseCase, lapTimeUseCase, trackWebMapper, trackRecordWebMapper);

    @Test
    void createTrackDelegatesWithMappedDomainObject() {
        CreateTrackRequest request = new CreateTrackRequest();
        request.setName(" Circuito del Jarama ");
        request.setShortName("jarama");
        request.setLocation(" Madrid ");
        request.setDescription(" Legendario ");

        when(trackUseCase.createTrack(any(Track.class))).thenReturn(track(1L, "Circuito del Jarama", "jarama"));

        TrackResponse response = trackController.createTrack(request);

        assertEquals(1L, response.getId());
        verify(trackUseCase).createTrack(argThat(track ->
                "Circuito del Jarama".equals(track.getName())
                        && "jarama".equals(track.getShortName())
                        && "Madrid".equals(track.getLocation())
                        && "Legendario".equals(track.getDescription())
        ));
    }

    @Test
    void updateTrackDelegatesWithMappedDomainObject() {
        UpdateTrackRequest request = new UpdateTrackRequest();
        request.setName(" Circuit de Barcelona ");
        request.setShortName("montmelo");
        request.setLocation(" Barcelona ");
        request.setDescription(" Tecnico ");

        when(trackUseCase.updateTrack(any(Long.class), any(Track.class))).thenReturn(track(2L, "Circuit de Barcelona", "montmelo"));

        TrackResponse response = trackController.updateTrack(2L, request);

        assertEquals("Circuit de Barcelona", response.getName());
        verify(trackUseCase).updateTrack(argThat(id -> id.equals(2L)), argThat(track ->
                "Circuit de Barcelona".equals(track.getName())
                        && "montmelo".equals(track.getShortName())
                        && "Barcelona".equals(track.getLocation())
                        && "Tecnico".equals(track.getDescription())
        ));
    }

    @Test
    void queryEndpointsMapResponsesAndLimitRanking() {
        when(trackUseCase.getAllTracks()).thenReturn(List.of(track(1L, "Jarama", "jarama")));
        when(trackUseCase.getTrackById(2L)).thenReturn(track(2L, "Montmelo", "montmelo"));
        when(lapTimeUseCase.getRankingByTrackId(1L)).thenReturn(List.of(
                lapTime(1L, "driver1", track(1L, "Jarama", "jarama"), 91000L),
                lapTime(2L, "driver2", track(1L, "Jarama", "jarama"), 92000L),
                lapTime(3L, "driver3", track(1L, "Jarama", "jarama"), 93000L)
        ));

        List<TrackResponse> allTracks = trackController.getAllTracks();
        TrackResponse byId = trackController.getTrackById(2L);
        List<TrackRecordResponse> ranking = trackController.getTrackRanking(1L, 2);

        assertEquals(1, allTracks.size());
        assertEquals("Jarama", allTracks.get(0).getName());
        assertEquals(2L, byId.getId());
        assertEquals(2, ranking.size());
        assertEquals("driver1", ranking.get(0).getUserDisplayName());
    }

    @Test
    void getTrackRankingRejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> trackController.getTrackRanking(1L, 0));
    }

    private Track track(Long id, String name, String shortName) {
        Track track = new Track();
        track.setId(id);
        track.setName(name);
        track.setShortName(shortName);
        track.setLocation("Spain");
        track.setDescription("Description");
        return track;
    }

    private LapTime lapTime(Long id, String displayName, Track track, Long lapTimeMs) {
        User user = new User();
        user.setId(id + 10);
        user.setDisplayName(displayName);

        LapTime lapTime = new LapTime();
        lapTime.setId(id);
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(LocalDate.of(2026, 4, 10));
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setVehicle("BMW M4");
        return lapTime;
    }
}
