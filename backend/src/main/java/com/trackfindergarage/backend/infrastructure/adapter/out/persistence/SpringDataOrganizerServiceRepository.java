package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.OrganizerService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link OrganizerService}.
 *
 * <p>Resuelve las consultas sobre servicios propios del organizador cargando el organizador, su
 * usuario y el servicio enlazado.</p>
 */
public interface SpringDataOrganizerServiceRepository extends JpaRepository<OrganizerService, Long> {

    @Override
    @EntityGraph(attributePaths = {"organizer", "organizer.user", "organizer.user.role", "service"})
    Optional<OrganizerService> findById(Long id);

    @EntityGraph(attributePaths = {"organizer", "organizer.user", "organizer.user.role", "service"})
    List<OrganizerService> findByOrganizerIdUser(Long organizerId);

    @EntityGraph(attributePaths = {"organizer", "organizer.user", "organizer.user.role", "service"})
    Optional<OrganizerService> findByOrganizerIdUserAndServiceId(Long organizerId, Long serviceId);
}
