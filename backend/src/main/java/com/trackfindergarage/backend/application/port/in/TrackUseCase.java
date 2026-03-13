package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Track;

import java.util.List;

public interface TrackUseCase {

    Track createTrack(Track track);

    Track updateTrack(Long id, Track track);

    void deleteTrack(Long id);

    List<Track> getAllTracks();

    Track getTrackById(Long id);
}