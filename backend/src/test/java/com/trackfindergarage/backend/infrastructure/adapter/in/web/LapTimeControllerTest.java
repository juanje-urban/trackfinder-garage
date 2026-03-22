package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.LapTimeResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.LapTimeWebMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LapTimeControllerTest {

    private final LapTimeUseCase lapTimeUseCase = mock(LapTimeUseCase.class);
    private final LapTimeWebMapper lapTimeWebMapper = new LapTimeWebMapper();
    private final LapTimeController lapTimeController = new LapTimeController(lapTimeUseCase, lapTimeWebMapper);

    @Test
    void createAndUpdateLapTimeDelegateToUseCaseAndReturnMappedResponse() {
        CreateLapTimeRequest createRequest = new CreateLapTimeRequest();
        createRequest.setUserId(1L);
        createRequest.setTrackId(2L);
        createRequest.setLapDate(LocalDate.now());
        createRequest.setLapTimeMs(90000L);
        createRequest.setVehicle("Car");

        UpdateLapTimeRequest updateRequest = new UpdateLapTimeRequest();
        updateRequest.setUserId(1L);
        updateRequest.setTrackId(2L);
        updateRequest.setLapDate(LocalDate.now());
        updateRequest.setLapTimeMs(89000L);
        updateRequest.setVehicle("Car 2");

        LapTime lapTime = lapTimeWithId(10L, 1L, 2L, 89000L);

        when(lapTimeUseCase.createLapTime(any(LapTime.class))).thenReturn(lapTime);
        when(lapTimeUseCase.updateLapTime(eq(10L), any(LapTime.class))).thenReturn(lapTime);

        LapTimeResponse created = lapTimeController.createLapTime(createRequest);
        LapTimeResponse updated = lapTimeController.updateLapTime(10L, updateRequest);

        assertEquals(10L, created.getId());
        assertEquals(89000L, updated.getLapTimeMs());
    }

    @Test
    void queryEndpointsMapUseCaseResult() {
        LapTime lapTime = lapTimeWithId(10L, 1L, 2L, 90000L);

        when(lapTimeUseCase.getAllLapTimes()).thenReturn(List.of(lapTime));
        when(lapTimeUseCase.getLapTimeById(10L)).thenReturn(lapTime);
        when(lapTimeUseCase.getLapTimesByUserId(1L)).thenReturn(List.of(lapTime));
        when(lapTimeUseCase.getLapTimesByTrackId(2L)).thenReturn(List.of(lapTime));
        when(lapTimeUseCase.getBestLapTimeByTrackId(2L)).thenReturn(lapTime);
        when(lapTimeUseCase.getBestLapTimeByUserIdAndTrackId(1L, 2L)).thenReturn(lapTime);
        when(lapTimeUseCase.getRankingByTrackId(2L)).thenReturn(List.of(lapTime));

        assertEquals(1, lapTimeController.getAllLapTimes().size());
        assertEquals(10L, lapTimeController.getLapTimeById(10L).getId());
        assertEquals(1, lapTimeController.getLapTimesByUserId(1L).size());
        assertEquals(1, lapTimeController.getLapTimesByTrackId(2L).size());
        assertEquals(10L, lapTimeController.getBestLapTimeByTrackId(2L).getId());
        assertEquals(10L, lapTimeController.getBestLapTimeByUserIdAndTrackId(1L, 2L).getId());
        assertEquals(1, lapTimeController.getRankingByTrackId(2L).size());
    }

    @Test
    void deleteLapTimeDelegatesToUseCase() {
        lapTimeController.deleteLapTime(10L);

        verify(lapTimeUseCase).deleteLapTime(10L);
    }

    private LapTime lapTimeWithId(Long id, Long userId, Long trackId, Long lapTimeMs) {
        User user = new User();
        user.setId(userId);
        user.setDisplayName("user");

        Track track = new Track();
        track.setId(trackId);
        track.setName("track");

        LapTime lapTime = new LapTime();
        lapTime.setId(id);
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(LocalDate.now());
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setVehicle("Car");
        return lapTime;
    }
}
