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

    private final TrackPersistencePort trackPersistencePort;

    public TrackService(TrackPersistencePort trackPersistencePort) {
        this.trackPersistencePort = trackPersistencePort;
    }

    @Override
    public Track createTrack(Track track) {
        return trackPersistencePort.save(track);
    }

    @Override
    public Track updateTrack(Long id, Track track) {
        Track existingTrack = getTrackById(id);

        existingTrack.setName(track.getName());
        existingTrack.setLocation(track.getLocation());
        existingTrack.setDescription(track.getDescription());

        return trackPersistencePort.save(existingTrack);
    }

    @Override
    public void deleteTrack(Long id) {
        Track existingTrack = getTrackById(id);
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
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + id));
    }
}