package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.LapTimeResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateLapTimeRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LapTimeWebMapperTest {

    private final LapTimeWebMapper lapTimeWebMapper = new LapTimeWebMapper();

    @Test
    void toDomainMapsCreateRequestToLapTime() {
        CreateLapTimeRequest request = new CreateLapTimeRequest();
        request.setUserId(1L);
        request.setTrackId(2L);
        request.setLapDate(LocalDate.of(2026, 3, 22));
        request.setLapTimeMs(90000L);
        request.setVehicle("Car");

        LapTime lapTime = lapTimeWebMapper.toDomain(request);

        assertEquals(1L, lapTime.getUser().getId());
        assertEquals(2L, lapTime.getTrack().getId());
        assertEquals(LocalDate.of(2026, 3, 22), lapTime.getLapDate());
        assertEquals(90000L, lapTime.getLapTimeMs());
        assertEquals("Car", lapTime.getVehicle());
    }

    @Test
    void updateDomainMapsUpdateRequestToExistingLapTime() {
        LapTime lapTime = new LapTime();
        UpdateLapTimeRequest request = new UpdateLapTimeRequest();
        request.setUserId(3L);
        request.setTrackId(4L);
        request.setLapDate(LocalDate.of(2026, 3, 21));
        request.setLapTimeMs(88000L);
        request.setVehicle("Car B");

        lapTimeWebMapper.updateDomain(lapTime, request);

        assertEquals(3L, lapTime.getUser().getId());
        assertEquals(4L, lapTime.getTrack().getId());
        assertEquals(LocalDate.of(2026, 3, 21), lapTime.getLapDate());
        assertEquals(88000L, lapTime.getLapTimeMs());
        assertEquals("Car B", lapTime.getVehicle());
    }

    @Test
    void toResponseMapsLapTimeToResponse() {
        User user = new User();
        user.setId(1L);
        user.setDisplayName("Juanje");

        Track track = new Track();
        track.setId(2L);
        track.setName("Jarama");

        LapTime lapTime = new LapTime();
        lapTime.setId(10L);
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(LocalDate.of(2026, 3, 20));
        lapTime.setLapTimeMs(87000L);
        lapTime.setVehicle("Car");

        LapTimeResponse response = lapTimeWebMapper.toResponse(lapTime);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("Juanje", response.getUserDisplayName());
        assertEquals(2L, response.getTrackId());
        assertEquals("Jarama", response.getTrackName());
        assertEquals(LocalDate.of(2026, 3, 20), response.getLapDate());
        assertEquals(87000L, response.getLapTimeMs());
        assertEquals("Car", response.getVehicle());
    }
}
