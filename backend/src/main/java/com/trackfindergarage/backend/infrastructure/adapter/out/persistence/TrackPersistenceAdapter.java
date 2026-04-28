package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.domain.model.Track;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que conecta el puerto de circuitos con Spring Data JPA.
 *
 * <p>Delega las operaciones CRUD y de búsqueda sobre circuitos en el repositorio concreto de
 * infraestructura.</p>
 */
@Component
public class TrackPersistenceAdapter implements TrackPersistencePort {

    private final SpringDataTrackRepository springDataTrackRepository;

    public TrackPersistenceAdapter(SpringDataTrackRepository springDataTrackRepository) {
        this.springDataTrackRepository = springDataTrackRepository;
    }

    @Override
    public Track save(Track track) {
        return springDataTrackRepository.save(track);
    }

    @Override
    public Optional<Track> findById(Long id) {
        return springDataTrackRepository.findById(id);
    }

    @Override
    public Optional<Track> findByName(String name) {
        return springDataTrackRepository.findByName(name);
    }

    @Override
    public Optional<Track> findByShortName(String shortName) {
        return springDataTrackRepository.findByShortName(shortName);
    }

    @Override
    public List<Track> findAll() {
        return springDataTrackRepository.findAll();
    }

    @Override
    public boolean existsById(Long id) {
        return springDataTrackRepository.existsById(id);
    }
}
