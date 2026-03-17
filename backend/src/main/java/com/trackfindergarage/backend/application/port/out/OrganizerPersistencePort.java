package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Organizer;

import java.util.List;
import java.util.Optional;

public interface OrganizerPersistencePort {

    Organizer save(Organizer organizer);

    Optional<Organizer> findById(Long id);

    List<Organizer> findAll();

    void delete(Organizer organizer);

    Optional<Organizer> findByLegalName(String legalName);

    Optional<Organizer> findByCif(String cif);
}