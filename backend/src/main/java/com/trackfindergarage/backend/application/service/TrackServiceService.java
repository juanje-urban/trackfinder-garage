package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.TrackServiceUseCase;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TrackServiceService implements TrackServiceUseCase {

    private final TrackServicePersistencePort trackServicePersistencePort;
    private final TrackPersistencePort trackPersistencePort;
    private final ServicePersistencePort servicePersistencePort;

    public TrackServiceService(TrackServicePersistencePort trackServicePersistencePort,
                               TrackPersistencePort trackPersistencePort,
                               ServicePersistencePort servicePersistencePort) {
        this.trackServicePersistencePort = trackServicePersistencePort;
        this.trackPersistencePort = trackPersistencePort;
        this.servicePersistencePort = servicePersistencePort;
    }

    @Override
    public TrackService createTrackService(TrackService trackService) {
        Long trackId = extractTrackId(trackService);
        Long serviceId = extractServiceId(trackService);

        trackServicePersistencePort.findByTrackIdAndServiceId(trackId, serviceId)
                .ifPresent(existingAssignment -> {
                    throw new DuplicateResourceException(
                            "Track service already exists for track id " + trackId
                                    + " and service id " + serviceId
                    );
                });

        Track track = trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        com.trackfindergarage.backend.domain.model.Service service = servicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        validateServiceAllowedForTrack(service);

        trackService.setTrack(track);
        trackService.setService(service);

        return trackServicePersistencePort.save(trackService);
    }

    @Override
    public void deleteTrackService(Long id) {
        TrackService trackService = findTrackServiceOrThrow(id);
        trackServicePersistencePort.delete(trackService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackService> getAllTrackServices() {
        return trackServicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TrackService getTrackServiceById(Long id) {
        return trackServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Track service not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackService> getTrackServicesByTrackId(Long trackId) {
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));

        return trackServicePersistencePort.findByTrackId(trackId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackService> getTrackServicesByServiceId(Long serviceId) {
        servicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        return trackServicePersistencePort.findByServiceId(serviceId);
    }

    private Long extractTrackId(TrackService trackService) {
        if (trackService.getTrack() == null || trackService.getTrack().getId() == null) {
            throw new IllegalArgumentException("Track id is required");
        }
        return trackService.getTrack().getId();
    }

    private Long extractServiceId(TrackService trackService) {
        if (trackService.getService() == null || trackService.getService().getId() == null) {
            throw new IllegalArgumentException("Service id is required");
        }
        return trackService.getService().getId();
    }

    private void validateServiceAllowedForTrack(com.trackfindergarage.backend.domain.model.Service service) {
        if (!Boolean.TRUE.equals(service.getAllowedForTrack())) {
            throw new IllegalArgumentException(
                    "Service with id " + service.getId() + " is not allowed for tracks"
            );
        }
    }

    private TrackService findTrackServiceOrThrow(Long id) {
        return trackServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Track service not found with id: " + id));
    }
}
