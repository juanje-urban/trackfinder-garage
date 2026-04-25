package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.TrackUseCase;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Track;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementa la lógica de gestión del catalogo de circuitos.
 *
 * <p>Valida datos obligatorios y normaliza textos.</p>
 */
@Service
@Transactional
public class TrackService implements TrackUseCase {

    private static final String TRACK_NOT_FOUND_WITH_ID = "Circuito no encontrado con id: ";
    private static final String NAME_REQUIRED = "El nombre es obligatorio";
    private static final String SHORT_NAME_REQUIRED = "El nombre corto es obligatorio";
    private static final String LOCATION_REQUIRED = "La ubicación es obligatoria";
    private static final String DESCRIPTION_REQUIRED = "La descripción es obligatoria";
    private static final String TRACK_NAME_ALREADY_EXISTS = "Ya existe un circuito con el nombre '%s'";
    private static final String TRACK_SHORT_NAME_ALREADY_EXISTS = "Ya existe un circuito con el nombre corto '%s'";

    private final TrackPersistencePort trackPersistencePort;

    public TrackService(TrackPersistencePort trackPersistencePort) {
        this.trackPersistencePort = trackPersistencePort;
    }

    /**
     * Crea un circuito tras validar sus datos básicos.
     *
     * @param track datos del circuito
     * @return circuito
     */
    @Override
    public Track createTrack(Track track) {
        validateTrack(track);
        normalizeTrack(track);
        validateUniqueFields(track, null);
        return trackPersistencePort.save(track);
    }

    /**
     * Actualiza un circuito existente.
     *
     * @param id identificador del circuito a modificar
     * @param track nuevos datos del circuito
     * @return circuito actualizado
     */
    @Override
    public Track updateTrack(Long id, Track track) {
        validateTrack(track);
        normalizeTrack(track);
        Track existingTrack = findTrackOrThrow(id);
        validateUniqueFields(track, id);

        existingTrack.setName(track.getName());
        existingTrack.setShortName(track.getShortName());
        existingTrack.setLocation(track.getLocation());
        existingTrack.setDescription(track.getDescription());

        return trackPersistencePort.save(existingTrack);
    }

    /**
     * Recupera todos los circuitos registrados.
     *
     * @return listado de circuitos
     */
    @Override
    @Transactional(readOnly = true)
    public List<Track> getAllTracks() {
        return trackPersistencePort.findAll();
    }

    /**
     * Recupera un circuito por su identificador.
     *
     * @param id identificador del circuito
     * @return circuito encontrado
     */
    @Override
    @Transactional(readOnly = true)
    public Track getTrackById(Long id) {
        return findTrackOrThrow(id);
    }

    /**
     * Variante interna de búsqueda que evita invocar un método proxificado desde el propio bean.
     *
     * @param id identificador del circuito
     * @return circuito encontrado
     */
    private Track findTrackOrThrow(Long id) {
        return trackPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + id));
    }

    //Validación básica de los datos
    private void validateTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track is required");
        }
        if (track.getName() == null || track.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(NAME_REQUIRED);
        }
        if (track.getShortName() == null || track.getShortName().trim().isEmpty()) {
            throw new IllegalArgumentException(SHORT_NAME_REQUIRED);
        }
        if (track.getLocation() == null || track.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException(LOCATION_REQUIRED);
        }
        if (track.getDescription() == null || track.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException(DESCRIPTION_REQUIRED);
        }
    }

    //Normaliza el nombre largo, el alias y la descripción
    private void normalizeTrack(Track track) {
        track.setName(track.getName().trim());
        track.setShortName(track.getShortName().trim());
        track.setLocation(track.getLocation().trim());
        track.setDescription(track.getDescription().trim());
    }

    //Comprueba valores repetidos
    private void validateUniqueFields(Track track, Long currentTrackId) {
        trackPersistencePort.findByName(track.getName())
                .filter(existingTrack -> !existingTrack.getId().equals(currentTrackId))
                .ifPresent(existingTrack -> {
                    throw new DuplicateResourceException(TRACK_NAME_ALREADY_EXISTS.formatted(track.getName()));
                });

        trackPersistencePort.findByShortName(track.getShortName())
                .filter(existingTrack -> !existingTrack.getId().equals(currentTrackId))
                .ifPresent(existingTrack -> {
                    throw new DuplicateResourceException(TRACK_SHORT_NAME_ALREADY_EXISTS.formatted(track.getShortName()));
                });
    }

}
