package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.LapTime;

import java.util.List;
import java.util.Optional;

public interface LapTimePersistencePort {

    LapTime save(LapTime lapTime);

    Optional<LapTime> findById(Long id);

    List<LapTime> findByUserId(Long userId);

    List<LapTime> findByTrackId(Long trackId);

    void delete(LapTime lapTime);
}
