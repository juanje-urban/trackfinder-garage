package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.TrackServicePersistencePort;
import com.trackfindergarage.backend.domain.model.TrackService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TrackServicePersistenceAdapter implements TrackServicePersistencePort {

    private final SpringDataTrackServiceRepository trackServiceRepository;

    public TrackServicePersistenceAdapter(SpringDataTrackServiceRepository trackServiceRepository) {
        this.trackServiceRepository = trackServiceRepository;
    }

    @Override
    public TrackService save(TrackService trackService) {
        return trackServiceRepository.save(trackService);
    }

    @Override
    public Optional<TrackService> findById(Long id) {
        return trackServiceRepository.findById(id);
    }

    @Override
    public List<TrackService> findAll() {
        return trackServiceRepository.findAll();
    }

    @Override
    public Optional<TrackService> findByTrackIdAndServiceId(Long trackId, Long serviceId) {
        return trackServiceRepository.findByTrackIdAndServiceId(trackId, serviceId);
    }

    @Override
    public void delete(TrackService trackService) {
        trackServiceRepository.delete(trackService);
    }
}
