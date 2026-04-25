package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.TrackService;

import java.util.List;

/**
 * Define los casos de uso para asignar servicios a circuitos.
 */
public interface TrackServiceUseCase {

    /**
     * Crea una nueva relación entre circuito y servicio.
     *
     * @param trackService asignación a persistir
     * @return asignación creada
     */
    TrackService createTrackService(TrackService trackService);

    /**
     * Elimina una asignación entre circuito y servicio.
     *
     * @param id identificador de la asignación
     */
    void deleteTrackService(Long id);

    /**
     * Recupera todas las relaciones existentes entre circuitos y servicios.
     *
     * @return listado de asignaciones
     */
    List<TrackService> getAllTrackServices();
}
