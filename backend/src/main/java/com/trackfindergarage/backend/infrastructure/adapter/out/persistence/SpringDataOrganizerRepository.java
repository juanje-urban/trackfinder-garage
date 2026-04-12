package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Organizer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataOrganizerRepository extends JpaRepository<Organizer, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "user.role"})
    java.util.List<Organizer> findAll();

    @Override
    @EntityGraph(attributePaths = {"user", "user.role"})
    Optional<Organizer> findById(Long id);

    Optional<Organizer> findByLegalName(String legalName);

    Optional<Organizer> findByCif(String cif);
}
