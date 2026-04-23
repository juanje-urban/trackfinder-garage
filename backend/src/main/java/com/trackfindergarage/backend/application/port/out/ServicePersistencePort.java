package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Service;

import java.util.List;
import java.util.Optional;

public interface ServicePersistencePort {

    Service save(Service service);

    Optional<Service> findById(Long id);

    Optional<Service> findByName(String name);

    List<Service> findAll();

    List<Service> findAllByAllowedForOrganizerTrue();
}
