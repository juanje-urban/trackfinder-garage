package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackRecordResponse;
import org.springframework.stereotype.Component;

@Component
public class TrackRecordWebMapper {

    public TrackRecordResponse toResponse(LapTime lapTime) {
        return TrackRecordResponse.builder()
                .trackId(lapTime.getTrack() != null ? lapTime.getTrack().getId() : null)
                .trackName(lapTime.getTrack() != null ? lapTime.getTrack().getName() : null)
                .userDisplayName(lapTime.getUser() != null ? lapTime.getUser().getDisplayName() : null)
                .lapDate(lapTime.getLapDate())
                .lapTimeMs(lapTime.getLapTimeMs())
                .vehicle(lapTime.getVehicle())
                .build();
    }
}
