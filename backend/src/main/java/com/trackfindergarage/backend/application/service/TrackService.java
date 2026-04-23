package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.TrackUseCase;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Track;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TrackService implements TrackUseCase {

    private static final String TRACK_NOT_FOUND_WITH_ID = "Circuito no encontrado con id: ";
    private static final String NAME_REQUIRED = "El nombre es obligatorio";
    private static final String SHORT_NAME_REQUIRED = "El nombre corto es obligatorio";
    private static final String LOCATION_REQUIRED = "La ubicación es obligatoria";
    private static final String DESCRIPTION_REQUIRED = "La descripción es obligatoria";
    private static final String TRACK_NAME_ALREADY_EXISTS = "Ya existe un circuito con el nombre '%s'";
    private static final String TRACK_SHORT_NAME_ALREADY_EXISTS = "Ya existe un circuito con el nombre corto '%s'";

    private final TrackPersistencePort trackPersistencePort;

    public TrackService(TrackPersistencePort trackPersistencePort) {
        this.trackPersistencePort = trackPersistencePort;
    }

    @Override
    public Track createTrack(Track track) {
        validateTrack(track);
        normalizeTrack(track);
        validateUniqueFields(track, null);
        return trackPersistencePort.save(track);
    }

    @Override
    public Track updateTrack(Long id, Track track) {
        validateTrack(track);
        normalizeTrack(track);
        Track existingTrack = findTrackOrThrow(id);
        validateUniqueFields(track, id);

        existingTrack.setName(track.getName());
        existingTrack.setShortName(track.getShortName());
        existingTrack.setLocation(track.getLocation());
        existingTrack.setDescription(track.getDescription());

        return trackPersistencePort.save(existingTrack);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Track> getAllTracks() {
        return trackPersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Track getTrackById(Long id) {
        return trackPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + id));
    }

    //Función privada que hace lo mismo que getTrackById. Los métodos con proxy de Spring no deben ser llamados desde dentro del propio bean. (Da error sonar)
    private Track findTrackOrThrow(Long id) {
        return trackPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + id));
    }

    private void validateTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track is required");
        }
        if (track.getName() == null || track.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(NAME_REQUIRED);
        }
        if (track.getShortName() == null || track.getShortName().trim().isEmpty()) {
            throw new IllegalArgumentException(SHORT_NAME_REQUIRED);
        }
        if (track.getLocation() == null || track.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException(LOCATION_REQUIRED);
        }
        if (track.getDescription() == null || track.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException(DESCRIPTION_REQUIRED);
        }
    }

    private void normalizeTrack(Track track) {
        track.setName(track.getName().trim());
        track.setShortName(track.getShortName().trim());
        track.setLocation(track.getLocation().trim());
        track.setDescription(track.getDescription().trim());
    }

    private void validateUniqueFields(Track track, Long currentTrackId) {
        trackPersistencePort.findByName(track.getName())
                .filter(existingTrack -> !existingTrack.getId().equals(currentTrackId))
                .ifPresent(existingTrack -> {
                    throw new DuplicateResourceException(TRACK_NAME_ALREADY_EXISTS.formatted(track.getName()));
                });

        trackPersistencePort.findByShortName(track.getShortName())
                .filter(existingTrack -> !existingTrack.getId().equals(currentTrackId))
                .ifPresent(existingTrack -> {
                    throw new DuplicateResourceException(TRACK_SHORT_NAME_ALREADY_EXISTS.formatted(track.getShortName()));
                });
    }

}
