package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.TrackServiceUseCase;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackServiceWebMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrackServiceControllerTest {

    private final TrackServiceUseCase trackServiceUseCase = mock(TrackServiceUseCase.class);
    private final TrackServiceWebMapper trackServiceWebMapper = new TrackServiceWebMapper();
    private final TrackServiceController trackServiceController =
            new TrackServiceController(trackServiceUseCase, trackServiceWebMapper);

    @Test
    void createTrackServiceDelegatesWithMappedDomainObject() {
        CreateTrackServiceRequest request = new CreateTrackServiceRequest();
        request.setTrackId(7L);
        request.setServiceId(4L);

        when(trackServiceUseCase.createTrackService(any(TrackService.class)))
                .thenReturn(trackService(9L, 7L, "Jarama", 4L, "Timing"));

        TrackServiceResponse response = trackServiceController.createTrackService(request);

        assertEquals(9L, response.getId());
        verify(trackServiceUseCase).createTrackService(argThat(trackService ->
                trackService.getTrack() != null
                        && trackService.getTrack().getId().equals(7L)
                        && trackService.getService() != null
                        && trackService.getService().getId().equals(4L)
        ));
    }

    @Test
    void deleteTrackServiceDelegatesToUseCase() {
        trackServiceController.deleteTrackService(9L);

        verify(trackServiceUseCase).deleteTrackService(9L);
    }

    @Test
    void getAllTrackServicesMapsResponses() {
        when(trackServiceUseCase.getAllTrackServices())
                .thenReturn(List.of(trackService(9L, 7L, "Jarama", 4L, "Timing")));

        List<TrackServiceResponse> responses = trackServiceController.getAllTrackServices();

        assertEquals(1, responses.size());
        assertEquals("Jarama", responses.get(0).getTrackName());
        assertEquals("Timing", responses.get(0).getServiceName());
    }

    private TrackService trackService(Long id, Long trackId, String trackName, Long serviceId, String serviceName) {
        Track track = new Track();
        track.setId(trackId);
        track.setName(trackName);

        com.trackfindergarage.backend.domain.model.Service service = new com.trackfindergarage.backend.domain.model.Service();
        service.setId(serviceId);
        service.setName(serviceName);

        TrackService trackService = new TrackService();
        trackService.setId(id);
        trackService.setTrack(track);
        trackService.setService(service);
        return trackService;
    }
}
