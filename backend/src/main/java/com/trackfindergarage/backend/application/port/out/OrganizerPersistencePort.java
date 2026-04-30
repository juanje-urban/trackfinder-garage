package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Organizer;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar organizadores.
 *
 * <p>Permite a la capa de aplicación trabajar con la identidad fiscal del organizador sin conocer cómo se almacena en
 * infraestructura.</p>
 */
public interface OrganizerPersistencePort {

    Organizer save(Organizer organizer);

    Optional<Organizer> findById(Long id);

    List<Organizer> findAll();

    void delete(Organizer organizer);

    Optional<Organizer> findByLegalName(String legalName);

    Optional<Organizer> findByCif(String cif);
}
