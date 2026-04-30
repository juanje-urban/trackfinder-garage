package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.OrganizerService;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir el catálogo propio de servicios de un organizador.
 *
 * <p>Modela las asociaciones entre organizadores y servicios que pueden ofrecer en sus eventos sin
 * exponer detalles de persistencia a la aplicación.</p>
 */
public interface OrganizerServicePersistencePort {

    OrganizerService save(OrganizerService organizerService);

    Optional<OrganizerService> findById(Long id);

    List<OrganizerService> findByOrganizerIdUser(Long organizerId);

    Optional<OrganizerService> findByOrganizerIdUserAndServiceId(Long organizerId, Long serviceId);
}
