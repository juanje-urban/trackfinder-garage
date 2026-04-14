package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.TrackServiceUseCase;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class TrackServiceService implements TrackServiceUseCase {

    private static final String TRACK_NOT_FOUND_WITH_ID = "Track not found with id: ";
    private static final String SERVICE_NOT_FOUND_WITH_ID = "Service not found with id: ";
    private static final String TRACK_SERVICE_NOT_FOUND_WITH_ID = "Track service not found with id: ";
    private static final String TRACK_ID_REQUIRED = "Track id is required";
    private static final String SERVICE_ID_REQUIRED = "Service id is required";
    private static final String TRACK_SERVICE_ALREADY_EXISTS =
            "Track service already exists for track id %d and service id %d";
    private static final String SERVICE_NOT_ALLOWED_FOR_TRACKS =
            "Service with id %d is not allowed for tracks";

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

        ensureTrackServiceDoesNotExist(trackId, serviceId);
        Track track = loadTrack(trackId);
        Service service = loadTrackAllowedService(serviceId);

        trackService.setTrack(track);
        trackService.setService(service);
        return trackServicePersistencePort.save(trackService);
    }

    @Override
    public void deleteTrackService(Long id) {
        trackServicePersistencePort.delete(findTrackServiceOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackService> getAllTrackServices() {
        return trackServicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TrackService getTrackServiceById(Long id) {
        return findTrackServiceOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackService> getTrackServicesByTrackId(Long trackId) {
        loadTrack(trackId);
        return trackServicePersistencePort.findByTrackId(trackId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackService> getTrackServicesByServiceId(Long serviceId) {
        loadService(serviceId);
        return trackServicePersistencePort.findByServiceId(serviceId);
    }

    private void ensureTrackServiceDoesNotExist(Long trackId, Long serviceId) {
        trackServicePersistencePort.findByTrackIdAndServiceId(trackId, serviceId)
                .ifPresent(existingAssignment -> {
                    throw new DuplicateResourceException(TRACK_SERVICE_ALREADY_EXISTS.formatted(trackId, serviceId));
                });
    }

    private Track loadTrack(Long trackId) {
        return trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));
    }

    private Service loadService(Long serviceId) {
        return servicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVICE_NOT_FOUND_WITH_ID + serviceId));
    }

    private Service loadTrackAllowedService(Long serviceId) {
        Service service = loadService(serviceId);

        if (!Boolean.TRUE.equals(service.getAllowedForTrack())) {
            throw new IllegalArgumentException(SERVICE_NOT_ALLOWED_FOR_TRACKS.formatted(service.getId()));
        }

        return service;
    }

    private Long extractTrackId(TrackService trackService) {
        if (trackService.getTrack() == null || trackService.getTrack().getId() == null) {
            throw new IllegalArgumentException(TRACK_ID_REQUIRED);
        }
        return trackService.getTrack().getId();
    }

    private Long extractServiceId(TrackService trackService) {
        if (trackService.getService() == null || trackService.getService().getId() == null) {
            throw new IllegalArgumentException(SERVICE_ID_REQUIRED);
        }
        return trackService.getService().getId();
    }

    private TrackService findTrackServiceOrThrow(Long id) {
        return trackServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_SERVICE_NOT_FOUND_WITH_ID + id));
    }
}
