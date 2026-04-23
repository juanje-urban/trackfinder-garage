package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.LapTimeResponse;
import org.springframework.stereotype.Component;

@Component
public class LapTimeWebMapper {

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
