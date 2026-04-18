package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataTrackRepository extends JpaRepository<Track, Long> {

    Optional<Track> findByName(String name);

    Optional<Track> findByShortName(String shortName);
}
