package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link EventService}.
 *
 * <p>Resuelve las lecturas de servicios de evento cargando las asociaciones necesarias para que la
 * capa de aplicación trabaje con el agregado ya hidratado.</p>
 */
public interface SpringDataEventServiceRepository extends JpaRepository<EventService, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "event",
            "trackService",
            "trackService.service",
            "organizerService",
            "organizerService.service"
    })
    Optional<EventService> findById(Long id);

    @EntityGraph(attributePaths = {
            "event",
            "trackService",
            "trackService.service",
            "organizerService",
            "organizerService.service"
    })
    List<EventService> findByEventId(Long eventId);

    @EntityGraph(attributePaths = {
            "event",
            "trackService",
            "trackService.service",
            "organizerService",
            "organizerService.service"
    })
    List<EventService> findByOrganizerServiceId(Long organizerServiceId);

    Optional<EventService> findByEventIdAndTrackServiceId(Long eventId, Long trackServiceId);

    Optional<EventService> findByEventIdAndOrganizerServiceId(Long eventId, Long organizerServiceId);
}
