package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateTrackRequest;
import org.springframework.stereotype.Component;

@Component
public class TrackWebMapper {

    public Track toDomain(CreateTrackRequest request) {
        Track track = new Track();
        track.setName(normalize(request.getName()));
        track.setLocation(normalize(request.getLocation()));
        track.setDescription(normalize(request.getDescription()));
        return track;
    }

    public void updateDomain(Track track, UpdateTrackRequest request) {
        track.setName(normalize(request.getName()));
        track.setLocation(normalize(request.getLocation()));
        track.setDescription(normalize(request.getDescription()));
    }

    public TrackResponse toResponse(Track track) {
        return TrackResponse.builder()
                .id(track.getId())
                .name(track.getName())
                .location(track.getLocation())
                .description(track.getDescription())
                .build();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
