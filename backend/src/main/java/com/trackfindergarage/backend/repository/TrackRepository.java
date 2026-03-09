package com.trackfindergarage.backend.repository;

import com.trackfindergarage.backend.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Integer> {
}
