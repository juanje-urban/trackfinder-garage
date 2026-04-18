package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackServiceResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackServiceWebMapperTest {

    private final TrackServiceWebMapper trackServiceWebMapper = new TrackServiceWebMapper();

    @Test
    void toDomainMapsCreateRequestToTrackService() {
        CreateTrackServiceRequest request = new CreateTrackServiceRequest();
        request.setTrackId(1L);
        request.setServiceId(2L);

        TrackService trackService = trackServiceWebMapper.toDomain(request);

        assertEquals(1L, trackService.getTrack().getId());
        assertEquals(2L, trackService.getService().getId());
    }

    @Test
    void toResponseMapsTrackServiceToResponse() {
        Track track = new Track();
        track.setId(1L);
        track.setName("Jarama");
        track.setShortName("jarama");

        com.trackfindergarage.backend.domain.model.Service service =
                new com.trackfindergarage.backend.domain.model.Service();
        service.setId(2L);
        service.setName("Parking");

        TrackService trackService = new TrackService();
        trackService.setId(10L);
        trackService.setTrack(track);
        trackService.setService(service);

        TrackServiceResponse response = trackServiceWebMapper.toResponse(trackService);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getTrackId());
        assertEquals("Jarama", response.getTrackName());
        assertEquals(2L, response.getServiceId());
        assertEquals("Parking", response.getServiceName());
    }
}
