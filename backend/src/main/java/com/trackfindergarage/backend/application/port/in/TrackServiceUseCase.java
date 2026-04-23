package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.TrackService;

import java.util.List;

public interface TrackServiceUseCase {

    TrackService createTrackService(TrackService trackService);

    void deleteTrackService(Long id);

    List<TrackService> getAllTrackServices();
}
