package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Track;

import java.util.List;
import java.util.Optional;

public interface TrackPersistencePort {

    Track save(Track track);

    Optional<Track> findById(Long id);

    List<Track> findAll();

    void delete(Track track);

    boolean existsById(Long id);
}