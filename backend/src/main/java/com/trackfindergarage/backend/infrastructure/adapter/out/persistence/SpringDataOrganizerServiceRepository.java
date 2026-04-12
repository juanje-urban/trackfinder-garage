package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.OrganizerService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataOrganizerServiceRepository extends JpaRepository<OrganizerService, Long> {

    @Override
    @EntityGraph(attributePaths = {"organizer", "organizer.user", "organizer.user.role", "service"})
    java.util.List<OrganizerService> findAll();

    @Override
    @EntityGraph(attributePaths = {"organizer", "organizer.user", "organizer.user.role", "service"})
    Optional<OrganizerService> findById(Long id);

    @EntityGraph(attributePaths = {"organizer", "organizer.user", "organizer.user.role", "service"})
    List<OrganizerService> findByOrganizerIdUser(Long organizerId);

    @EntityGraph(attributePaths = {"organizer", "organizer.user", "organizer.user.role", "service"})
    List<OrganizerService> findByServiceId(Long serviceId);

    @EntityGraph(attributePaths = {"organizer", "organizer.user", "organizer.user.role", "service"})
    Optional<OrganizerService> findByOrganizerIdUserAndServiceId(Long organizerId, Long serviceId);
}
