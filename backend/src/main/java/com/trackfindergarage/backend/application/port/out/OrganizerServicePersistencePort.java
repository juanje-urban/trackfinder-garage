package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.OrganizerService;

import java.util.List;
import java.util.Optional;

public interface OrganizerServicePersistencePort {

    OrganizerService save(OrganizerService organizerService);

    Optional<OrganizerService> findById(Long id);

    List<OrganizerService> findByOrganizerIdUser(Long organizerId);

    Optional<OrganizerService> findByOrganizerIdUserAndServiceId(Long organizerId, Long serviceId);
}
