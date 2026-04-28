package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Service;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar servicios del catálogo.
 *
 * <p>Permite a la aplicación recuperar servicios generales y filtrar aquellos que pueden ofrecerse
 * desde el workspace del organizador.</p>
 */
public interface ServicePersistencePort {

    Service save(Service service);

    Optional<Service> findById(Long id);

    Optional<Service> findByName(String name);

    List<Service> findAll();

    List<Service> findAllByAllowedForOrganizerTrue();
}
