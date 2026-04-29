package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.LapTimePersistencePort;
import com.trackfindergarage.backend.domain.model.LapTime;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto de tiempos de vuelta.
 *
 * <p>Delega en Spring Data JPA las lecturas y escrituras del histórico de vueltas por usuario y por
 * circuito.</p>
 */
@Component
public class LapTimePersistenceAdapter implements LapTimePersistencePort {

    private final SpringDataLapTimeRepository lapTimeRepository;

    public LapTimePersistenceAdapter(SpringDataLapTimeRepository lapTimeRepository) {
        this.lapTimeRepository = lapTimeRepository;
    }

    @Override
    public LapTime save(LapTime lapTime) {
        return lapTimeRepository.save(lapTime);
    }

    @Override
    public Optional<LapTime> findById(Long id) {
        return lapTimeRepository.findById(id);
    }

    @Override
    public List<LapTime> findByUserId(Long userId) {
        return lapTimeRepository.findByUserId(userId);
    }

    @Override
    public List<LapTime> findByTrackId(Long trackId) {
        return lapTimeRepository.findByTrackId(trackId);
    }

    @Override
    public void delete(LapTime lapTime) {
        lapTimeRepository.delete(lapTime);
    }
}
