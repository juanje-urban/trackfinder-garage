package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.TrackService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataTrackServiceRepository extends JpaRepository<TrackService, Long> {

    List<TrackService> findByTrackId(Long trackId);

    List<TrackService> findByServiceId(Long serviceId);

    Optional<TrackService> findByTrackIdAndServiceId(Long trackId, Long serviceId);
}
