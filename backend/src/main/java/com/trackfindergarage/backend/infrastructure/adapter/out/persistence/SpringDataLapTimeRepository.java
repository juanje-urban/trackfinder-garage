package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.LapTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataLapTimeRepository extends JpaRepository<LapTime, Long> {

    List<LapTime> findByUserId(Long userId);

    List<LapTime> findByTrackId(Long trackId);

    List<LapTime> findByUserIdAndTrackId(Long userId, Long trackId);
}
