package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateTrackRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackWebMapperTest {

    private final TrackWebMapper trackWebMapper = new TrackWebMapper();

    @Test
    void toDomainMapsCreateRequestToTrack() {
        CreateTrackRequest request = new CreateTrackRequest();
        request.setName("Jarama");
        request.setLocation("Madrid");
        request.setDescription("Fast track");

        Track track = trackWebMapper.toDomain(request);

        assertEquals("Jarama", track.getName());
        assertEquals("Madrid", track.getLocation());
        assertEquals("Fast track", track.getDescription());
    }

    @Test
    void updateDomainMapsUpdateRequestToExistingTrack() {
        Track track = new Track();
        UpdateTrackRequest request = new UpdateTrackRequest();
        request.setName("Montmelo");
        request.setLocation("Barcelona");
        request.setDescription("Updated");

        trackWebMapper.updateDomain(track, request);

        assertEquals("Montmelo", track.getName());
        assertEquals("Barcelona", track.getLocation());
        assertEquals("Updated", track.getDescription());
    }

    @Test
    void toResponseMapsTrackToResponse() {
        Track track = new Track();
        track.setId(4L);
        track.setName("Cheste");
        track.setLocation("Valencia");
        track.setDescription("Circuit");

        TrackResponse response = trackWebMapper.toResponse(track);

        assertEquals(4L, response.getId());
        assertEquals("Cheste", response.getName());
        assertEquals("Valencia", response.getLocation());
        assertEquals("Circuit", response.getDescription());
    }
}
