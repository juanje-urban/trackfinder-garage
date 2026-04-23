package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOwnLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.LapTimeResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.LapTimeWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LapTimeControllerTest {

    private final LapTimeUseCase lapTimeUseCase = mock(LapTimeUseCase.class);
    private final LapTimeWebMapper lapTimeWebMapper = new LapTimeWebMapper();
    private final LapTimeController lapTimeController = new LapTimeController(lapTimeUseCase, lapTimeWebMapper);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void createCurrentUserLapTimeDelegatesToUseCaseAndMapsResponse() {
        CreateOwnLapTimeRequest request = new CreateOwnLapTimeRequest();
        request.setTrackId(7L);
        request.setLapDate(LocalDate.of(2026, 4, 10));
        request.setLapTimeMs(91234L);
        request.setVehicle("BMW M4");

        when(authentication.getName()).thenReturn("driver@example.com");
        when(lapTimeUseCase.createLapTimeForAuthenticatedUser(
                "driver@example.com",
                7L,
                LocalDate.of(2026, 4, 10),
                91234L,
                "BMW M4"
        )).thenReturn(lapTime(5L, 1L, "driver", 7L, "Jarama"));

        LapTimeResponse response = lapTimeController.createCurrentUserLapTime(request, authentication);

        assertEquals(5L, response.getId());
        assertEquals("Jarama", response.getTrackName());
    }

    @Test
    void deleteCurrentUserLapTimeDelegatesToUseCase() {
        when(authentication.getName()).thenReturn("driver@example.com");

        lapTimeController.deleteCurrentUserLapTime(5L, authentication);

        verify(lapTimeUseCase).deleteOwnLapTime("driver@example.com", 5L);
    }

    @Test
    void queryEndpointsMapLapTimes() {
        when(authentication.getName()).thenReturn("driver@example.com");
        when(lapTimeUseCase.getLapTimesByAuthenticatedEmail("driver@example.com"))
                .thenReturn(List.of(lapTime(5L, 1L, "driver", 7L, "Jarama")));
        when(lapTimeUseCase.getLapTimesByUserId(2L))
                .thenReturn(List.of(lapTime(6L, 2L, "organizer", 8L, "Montmeló")));

        List<LapTimeResponse> ownResponses = lapTimeController.getCurrentUserLapTimes(authentication);
        List<LapTimeResponse> userResponses = lapTimeController.getLapTimesByUserId(2L);

        assertEquals(1, ownResponses.size());
        assertEquals("driver", ownResponses.get(0).getUserDisplayName());
        assertEquals(1, userResponses.size());
        assertEquals("Montmeló", userResponses.get(0).getTrackName());
    }

    private LapTime lapTime(Long id,
                            Long userId,
                            String userDisplayName,
                            Long trackId,
                            String trackName) {
        User user = new User();
        user.setId(userId);
        user.setDisplayName(userDisplayName);

        Track track = new Track();
        track.setId(trackId);
        track.setName(trackName);

        LapTime lapTime = new LapTime();
        lapTime.setId(id);
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(LocalDate.of(2026, 4, 10));
        lapTime.setLapTimeMs(91234L);
        lapTime.setVehicle("BMW M4");
        return lapTime;
    }
}
