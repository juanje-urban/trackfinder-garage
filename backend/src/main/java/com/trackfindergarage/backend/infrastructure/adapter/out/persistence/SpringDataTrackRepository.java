package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTrackRepository extends JpaRepository<Track, Long> {
}