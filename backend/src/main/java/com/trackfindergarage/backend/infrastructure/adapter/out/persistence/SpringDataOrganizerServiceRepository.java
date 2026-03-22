package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.OrganizerService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataOrganizerServiceRepository extends JpaRepository<OrganizerService, Long> {

    List<OrganizerService> findByOrganizerIdUser(Long organizerId);

    List<OrganizerService> findByServiceId(Long serviceId);

    Optional<OrganizerService> findByOrganizerIdUserAndServiceId(Long organizerId, Long serviceId);
}
