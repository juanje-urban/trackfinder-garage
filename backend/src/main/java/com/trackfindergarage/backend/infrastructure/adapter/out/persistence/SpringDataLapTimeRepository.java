package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.LapTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link LapTime}.
 *
 * <p>Resuelve consultas sobre tiempos de vuelta cargando el usuario y el circuito asociados a cada
 * registro.</p>
 */
public interface SpringDataLapTimeRepository extends JpaRepository<LapTime, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "track"})
    Optional<LapTime> findById(Long id);

    @EntityGraph(attributePaths = {"user", "track"})
    List<LapTime> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "track"})
    List<LapTime> findByTrackId(Long trackId);

    @EntityGraph(attributePaths = {"user", "track"})
    List<LapTime> findByUserIdAndTrackId(Long userId, Long trackId);
}
