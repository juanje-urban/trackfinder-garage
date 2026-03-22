package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.LapTimeResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateLapTimeRequest;
import org.springframework.stereotype.Component;

@Component
public class LapTimeWebMapper {

    public LapTime toDomain(CreateLapTimeRequest request) {
        User user = new User();
        user.setId(request.getUserId());

        Track track = new Track();
        track.setId(request.getTrackId());

        LapTime lapTime = new LapTime();
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(request.getLapDate());
        lapTime.setLapTimeMs(request.getLapTimeMs());
        lapTime.setVehicle(request.getVehicle());

        return lapTime;
    }

    public void updateDomain(LapTime lapTime, UpdateLapTimeRequest request) {
        User user = new User();
        user.setId(request.getUserId());

        Track track = new Track();
        track.setId(request.getTrackId());

        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(request.getLapDate());
        lapTime.setLapTimeMs(request.getLapTimeMs());
        lapTime.setVehicle(request.getVehicle());
    }

    public LapTimeResponse toResponse(LapTime lapTime) {
        return LapTimeResponse.builder()
                .id(lapTime.getId())
                .userId(lapTime.getUser() != null ? lapTime.getUser().getId() : null)
                .userDisplayName(lapTime.getUser() != null ? lapTime.getUser().getDisplayName() : null)
                .trackId(lapTime.getTrack() != null ? lapTime.getTrack().getId() : null)
                .trackName(lapTime.getTrack() != null ? lapTime.getTrack().getName() : null)
                .lapDate(lapTime.getLapDate())
                .lapTimeMs(lapTime.getLapTimeMs())
                .vehicle(lapTime.getVehicle())
                .build();
    }
}
