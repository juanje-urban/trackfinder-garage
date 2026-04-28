package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.domain.model.Service;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto de servicios mediante Spring Data JPA.
 *
 * <p>Encapsula el acceso al repositorio de servicios y expone a la aplicación una interfaz estable
 * independiente de JPA.</p>
 */
@Component
public class ServicePersistenceAdapter implements ServicePersistencePort {

    private final SpringDataServiceRepository springDataServiceRepository;

    public ServicePersistenceAdapter(SpringDataServiceRepository springDataServiceRepository) {
        this.springDataServiceRepository = springDataServiceRepository;
    }

    @Override
    public Service save(Service service) {
        return springDataServiceRepository.save(service);
    }

    @Override
    public Optional<Service> findById(Long id) {
        return springDataServiceRepository.findById(id);
    }

    @Override
    public Optional<Service> findByName(String name) {
        return springDataServiceRepository.findByName(name);
    }

    @Override
    public List<Service> findAll() {
        return springDataServiceRepository.findAll();
    }

    @Override
    public List<Service> findAllByAllowedForOrganizerTrue() {
        return springDataServiceRepository.findAllByAllowedForOrganizerTrue();
    }
}
