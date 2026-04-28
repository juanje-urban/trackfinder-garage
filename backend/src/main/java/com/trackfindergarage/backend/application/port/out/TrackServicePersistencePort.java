package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.TrackService;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir asociaciones entre circuitos y servicios.
 *
 * <p>Modela el catálogo específico de servicios disponibles en cada circuito sin acoplar la
 * aplicación a la infraestructura de persistencia.</p>
 */
public interface TrackServicePersistencePort {

    TrackService save(TrackService trackService);

    Optional<TrackService> findById(Long id);

    List<TrackService> findAll();

    Optional<TrackService> findByTrackIdAndServiceId(Long trackId, Long serviceId);

    void delete(TrackService trackService);
}
