package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link Service}.
 *
 * <p>Expone las consultas específicas del catálogo de servicios usadas por la capa de
 * infraestructura.</p>
 */
public interface SpringDataServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findByName(String name);

    List<Service> findAllByAllowedForOrganizerTrue();
}
