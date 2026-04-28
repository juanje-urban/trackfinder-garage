package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.LapTime;

import java.util.List;

/**
 * Define los casos de uso relacionados con los tiempos de vuelta de los usuarios.
 */
public interface LapTimeUseCase {

    /**
     * Registra un nuevo tiempo de vuelta para el usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param trackId identificador del circuito
     * @param lapDate fecha en la que se realizó la vuelta
     * @param lapTimeMs tiempo de vuelta en milisegundos
     * @param vehicle vehículo utilizado
     * @return tiempo de vuelta creado
     */
    LapTime createLapTimeForAuthenticatedUser(String authenticatedEmail,
                                              Long trackId,
                                              java.time.LocalDate lapDate,
                                              Long lapTimeMs,
                                              String vehicle);

    /**
     * Elimina un tiempo de vuelta.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param id identificador del tiempo de vuelta
     */
    void deleteOwnLapTime(String authenticatedEmail, Long id);

    /**
     * Recupera los tiempos de vuelta del usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de tiempos del usuario
     */
    List<LapTime> getLapTimesByAuthenticatedEmail(String authenticatedEmail);

    /**
     * Recupera los tiempos de vuelta de un usuario concreto.
     *
     * @param userId identificador del usuario
     * @return listado de tiempos del usuario
     */
    List<LapTime> getLapTimesByUserId(Long userId);

    /**
     * Recupera el ranking de tiempos de un circuito.
     *
     * @param trackId identificador del circuito
     * @return ranking de tiempos del circuito
     */
    List<LapTime> getRankingByTrackId(Long trackId);
}
