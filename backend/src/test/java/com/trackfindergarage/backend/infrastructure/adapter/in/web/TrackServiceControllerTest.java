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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrackServiceControllerTest {

    private final TrackServiceUseCase trackServiceUseCase = mock(TrackServiceUseCase.class);
    private final TrackServiceWebMapper trackServiceWebMapper = new TrackServiceWebMapper();
    private final TrackServiceController trackServiceController =
            new TrackServiceController(trackServiceUseCase, trackServiceWebMapper);

    @Test
    void createTrackServiceDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateTrackServiceRequest request = new CreateTrackServiceRequest();
        request.setTrackId(1L);
        request.setServiceId(2L);

        when(trackServiceUseCase.createTrackService(any(TrackService.class)))
                .thenReturn(trackServiceWithIds(10L, 1L, 2L));

        TrackServiceResponse response = trackServiceController.createTrackService(request);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getTrackId());
        assertEquals(2L, response.getServiceId());
    }

    @Test
    void listingEndpointsMapUseCaseResult() {
        TrackService trackService = trackServiceWithIds(10L, 1L, 2L);

        when(trackServiceUseCase.getAllTrackServices()).thenReturn(List.of(trackService));
        when(trackServiceUseCase.getTrackServicesByTrackId(1L)).thenReturn(List.of(trackService));
        when(trackServiceUseCase.getTrackServicesByServiceId(2L)).thenReturn(List.of(trackService));
        when(trackServiceUseCase.getTrackServiceById(10L)).thenReturn(trackService);

        assertEquals(1, trackServiceController.getAllTrackServices().size());
        assertEquals(1, trackServiceController.getTrackServicesByTrackId(1L).size());
        assertEquals(1, trackServiceController.getTrackServicesByServiceId(2L).size());
        assertEquals(10L, trackServiceController.getTrackServiceById(10L).getId());
    }

    @Test
    void deleteTrackServiceDelegatesToUseCase() {
        trackServiceController.deleteTrackService(10L);

        verify(trackServiceUseCase).deleteTrackService(10L);
    }

    private TrackService trackServiceWithIds(Long id, Long trackId, Long serviceId) {
        Track track = new Track();
        track.setId(trackId);
        track.setName("Track");

        com.trackfindergarage.backend.domain.model.Service service = new com.trackfindergarage.backend.domain.model.Service();
        service.setId(serviceId);
        service.setName("Parking");

        TrackService trackService = new TrackService();
        trackService.setId(id);
        trackService.setTrack(track);
        trackService.setService(service);
        return trackService;
    }
}
