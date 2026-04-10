package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.TrackUseCase;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Track;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TrackService implements TrackUseCase {

    private static final String TRACK_NOT_FOUND_WITH_ID = "Track not found with id: ";
    private static final String NAME_REQUIRED = "Name is required";
    private static final String LOCATION_REQUIRED = "Location is required";
    private static final String DESCRIPTION_REQUIRED = "Description is required";

    private final TrackPersistencePort trackPersistencePort;

    public TrackService(TrackPersistencePort trackPersistencePort) {
        this.trackPersistencePort = trackPersistencePort;
    }

    @Override
    public Track createTrack(Track track) {
        validateTrack(track);
        track.setName(track.getName().trim());
        track.setLocation(track.getLocation().trim());
        track.setDescription(track.getDescription().trim());
        return trackPersistencePort.save(track);
    }

    @Override
    public Track updateTrack(Long id, Track track) {
        validateTrack(track);
        Track existingTrack = findTrackOrThrow(id);

        existingTrack.setName(track.getName().trim());
        existingTrack.setLocation(track.getLocation().trim());
        existingTrack.setDescription(track.getDescription().trim());

        return trackPersistencePort.save(existingTrack);
    }

    @Override
    public void deleteTrack(Long id) {
        Track existingTrack = findTrackOrThrow(id);
        trackPersistencePort.delete(existingTrack);
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
        if (track.getName() == null || track.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(NAME_REQUIRED);
        }
        if (track.getLocation() == null || track.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException(LOCATION_REQUIRED);
        }
        if (track.getDescription() == null || track.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException(DESCRIPTION_REQUIRED);
        }
    }

}
