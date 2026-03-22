package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.TrackService;

import java.util.List;

public interface TrackServiceUseCase {

    TrackService createTrackService(TrackService trackService);

    void deleteTrackService(Long id);

    List<TrackService> getAllTrackServices();

    TrackService getTrackServiceById(Long id);

    List<TrackService> getTrackServicesByTrackId(Long trackId);

    List<TrackService> getTrackServicesByServiceId(Long serviceId);
}
