package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.LapTime;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar tiempos de vuelta.
 *
 * <p>Permite recuperar vueltas por usuario o por circuito y mantener el histórico de registros sin
 * exponer detalles de JPA a la capa de aplicación.</p>
 */
public interface LapTimePersistencePort {

    LapTime save(LapTime lapTime);

    Optional<LapTime> findById(Long id);

    List<LapTime> findByUserId(Long userId);

    List<LapTime> findByTrackId(Long trackId);

    void delete(LapTime lapTime);
}
