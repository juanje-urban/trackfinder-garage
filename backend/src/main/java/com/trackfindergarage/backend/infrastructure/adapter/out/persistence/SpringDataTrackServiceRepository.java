package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.TrackService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link TrackService}.
 *
 * <p>Aplica {@code EntityGraph} en las lecturas para devolver la asociación circuito-servicio con
 * sus referencias cargadas y evitar accesos innecesarios.</p>
 */
public interface SpringDataTrackServiceRepository extends JpaRepository<TrackService, Long> {

    @Override
    @EntityGraph(attributePaths = {"track", "service"})
    List<TrackService> findAll();

    @Override
    @EntityGraph(attributePaths = {"track", "service"})
    Optional<TrackService> findById(Long id);

    @EntityGraph(attributePaths = {"track", "service"})
    Optional<TrackService> findByTrackIdAndServiceId(Long trackId, Long serviceId);
}
