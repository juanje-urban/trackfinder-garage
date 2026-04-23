package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.TrackService;

import java.util.List;
import java.util.Optional;

public interface TrackServicePersistencePort {

    TrackService save(TrackService trackService);

    Optional<TrackService> findById(Long id);

    List<TrackService> findAll();

    Optional<TrackService> findByTrackIdAndServiceId(Long trackId, Long serviceId);

    void delete(TrackService trackService);
}
