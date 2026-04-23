package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Track;

import java.util.List;
import java.util.Optional;

public interface TrackPersistencePort {

    Track save(Track track);

    Optional<Track> findById(Long id);

    Optional<Track> findByName(String name);

    Optional<Track> findByShortName(String shortName);

    List<Track> findAll();

    boolean existsById(Long id);
}
