package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Track;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar circuitos del catálogo.
 *
 * <p>Aísla a la capa de aplicación de la tecnología concreta usada para guardar los circuitos y
 * expone sólo las operaciones que necesita el dominio.</p>
 */
public interface TrackPersistencePort {

    Track save(Track track);

    Optional<Track> findById(Long id);

    Optional<Track> findByName(String name);

    Optional<Track> findByShortName(String shortName);

    List<Track> findAll();

    boolean existsById(Long id);
}
