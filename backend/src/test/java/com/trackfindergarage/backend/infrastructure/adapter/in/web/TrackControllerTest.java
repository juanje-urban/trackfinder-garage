package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.application.port.in.TrackUseCase;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackRecordResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackRecordWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackWebMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrackControllerTest {

    private final TrackUseCase trackUseCase = mock(TrackUseCase.class);
    private final LapTimeUseCase lapTimeUseCase = mock(LapTimeUseCase.class);
    private final TrackWebMapper trackWebMapper = new TrackWebMapper();
    private final TrackRecordWebMapper trackRecordWebMapper = new TrackRecordWebMapper();
    private final TrackController trackController = new TrackController(
            trackUseCase,
            lapTimeUseCase,
            trackWebMapper,
            trackRecordWebMapper
    );

    @Test
    void createTrackDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateTrackRequest request = new CreateTrackRequest();
        request.setName("Jarama");
        request.setLocation("Madrid");
        request.setDescription("Circuit");

        when(trackUseCase.createTrack(any(Track.class))).thenReturn(trackWithId(1L, "Jarama"));

        TrackResponse response = trackController.createTrack(request);

        assertEquals(1L, response.getId());
        assertEquals("Jarama", response.getName());
        verify(trackUseCase).createTrack(any(Track.class));
    }

    @Test
    void updateTrackDelegatesToUseCaseAndReturnsMappedResponse() {
        UpdateTrackRequest request = new UpdateTrackRequest();
        request.setName("Cheste");
        request.setLocation("Valencia");
        request.setDescription("Updated");

        when(trackUseCase.updateTrack(eq(2L), any(Track.class))).thenReturn(trackWithId(2L, "Cheste"));

        TrackResponse response = trackController.updateTrack(2L, request);

        assertEquals(2L, response.getId());
        assertEquals("Cheste", response.getName());
        verify(trackUseCase).updateTrack(eq(2L), any(Track.class));
    }

    @Test
    void getAllTracksMapsUseCaseResult() {
        when(trackUseCase.getAllTracks()).thenReturn(List.of(trackWithId(1L, "Jarama")));

        List<TrackResponse> response = trackController.getAllTracks();

        assertEquals(1, response.size());
        assertEquals("Jarama", response.getFirst().getName());
    }

    @Test
    void getTrackByIdReturnsMappedResponse() {
        when(trackUseCase.getTrackById(3L)).thenReturn(trackWithId(3L, "Montmelo"));

        TrackResponse response = trackController.getTrackById(3L);

        assertEquals(3L, response.getId());
        assertEquals("Montmelo", response.getName());
    }

    @Test
    void getTrackRecordReturnsMappedPublicLapRecord() {
        when(lapTimeUseCase.getBestLapTimeByTrackId(3L)).thenReturn(bestLapTimeForTrack(3L, "Montmelo"));

        TrackRecordResponse response = trackController.getTrackRecord(3L);

        assertEquals(3L, response.getTrackId());
        assertEquals("Montmelo", response.getTrackName());
        assertEquals("apexhunter", response.getUserDisplayName());
        assertEquals(87234L, response.getLapTimeMs());
        assertEquals("Porsche 718 Cayman GT4", response.getVehicle());
        verify(lapTimeUseCase).getBestLapTimeByTrackId(3L);
    }

    @Test
    void deleteTrackDelegatesToUseCase() {
        trackController.deleteTrack(4L);

        verify(trackUseCase).deleteTrack(4L);
    }

    private Track trackWithId(Long id, String name) {
        Track track = new Track();
        track.setId(id);
        track.setName(name);
        track.setLocation("Location");
        track.setDescription("Description");
        return track;
    }

    private LapTime bestLapTimeForTrack(Long trackId, String trackName) {
        User user = new User();
        user.setId(11L);
        user.setDisplayName("apexhunter");

        Track track = trackWithId(trackId, trackName);

        LapTime lapTime = new LapTime();
        lapTime.setId(21L);
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(LocalDate.of(2026, 4, 5));
        lapTime.setLapTimeMs(87234L);
        lapTime.setVehicle("Porsche 718 Cayman GT4");
        return lapTime;
    }
}
