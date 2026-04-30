package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.domain.model.Organizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que conecta el puerto de organizadores con Spring Data JPA.
 *
 * <p>Centraliza el acceso a los datos del organizador y sus búsquedas por identificadores de negocio como CIF o razón
 * social.</p>
 */
@Component
public class OrganizerPersistenceAdapter implements OrganizerPersistencePort {

    private final SpringDataOrganizerRepository organizerRepository;

    public OrganizerPersistenceAdapter(SpringDataOrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
    }

    @Override
    public Organizer save(Organizer organizer) {
        return organizerRepository.save(organizer);
    }

    @Override
    public Optional<Organizer> findById(Long id) {
        return organizerRepository.findById(id);
    }

    @Override
    public List<Organizer> findAll() {
        return organizerRepository.findAll();
    }

    @Override
    public void delete(Organizer organizer) {
        organizerRepository.delete(organizer);
    }

    @Override
    public Optional<Organizer> findByLegalName(String legalName) {
        return organizerRepository.findByLegalName(legalName);
    }

    @Override
    public Optional<Organizer> findByCif(String cif) {
        return organizerRepository.findByCif(cif);
    }
}
