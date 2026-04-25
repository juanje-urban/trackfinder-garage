package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Track;

import java.util.List;

/**
 * Define los casos de uso relacionados con la gestion de circuitos.
 */
public interface TrackUseCase {

    /**
     * Crea un nuevo circuito.
     *
     * @param track datos del circuito
     * @return circuito persistido
     */
    Track createTrack(Track track);

    /**
     * Actualiza un circuito existente.
     *
     * @param id identificador del circuito a modificar
     * @param track nuevos datos del circuito
     * @return circuito actualizado
     */
    Track updateTrack(Long id, Track track);

    /**
     * Recupera todos los circuitos registrados.
     *
     * @return listado de circuitos
     */
    List<Track> getAllTracks();

    /**
     * Recupera un circuito por su identificador.
     *
     * @param id identificador del circuito
     * @return circuito encontrado
     */
    Track getTrackById(Long id);
}
