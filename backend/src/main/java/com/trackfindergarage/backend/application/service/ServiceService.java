package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.ServiceUseCase;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServiceService implements ServiceUseCase {

    private final ServicePersistencePort servicePersistencePort;

    public ServiceService(ServicePersistencePort servicePersistencePort) {
        this.servicePersistencePort = servicePersistencePort;
    }

    @Override
    public com.trackfindergarage.backend.domain.model.Service createService(
            com.trackfindergarage.backend.domain.model.Service service
    ) {
        servicePersistencePort.findByName(service.getName())
                .ifPresent(existingService -> {
                    throw new DuplicateResourceException(
                            "Service with name '" + service.getName() + "' already exists"
                    );
                });

        service.setEnabled(true);
        return servicePersistencePort.save(service);
    }

    @Override
    public com.trackfindergarage.backend.domain.model.Service updateService(
            Long id,
            com.trackfindergarage.backend.domain.model.Service service
    ) {
        com.trackfindergarage.backend.domain.model.Service existingService = findServiceOrThrow(id);

        servicePersistencePort.findByName(service.getName())
                .ifPresent(foundService -> {
                    if (!foundService.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                "Service with name '" + service.getName() + "' already exists"
                        );
                    }
                });

        existingService.setName(service.getName());
        existingService.setDescription(service.getDescription());
        existingService.setAllowedForTrack(service.getAllowedForTrack());
        existingService.setAllowedForOrganizer(service.getAllowedForOrganizer());

        return servicePersistencePort.save(existingService);
    }

    @Override
    public void deleteService(Long id) {
        com.trackfindergarage.backend.domain.model.Service existingService = findServiceOrThrow(id);
        servicePersistencePort.delete(existingService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.trackfindergarage.backend.domain.model.Service> getAllServices() {
        return servicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.trackfindergarage.backend.domain.model.Service> getAllServicesAllowedForTrack() {
        return servicePersistencePort.findAllByAllowedForTrackTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.trackfindergarage.backend.domain.model.Service> getAllServicesAllowedForOrganizer() {
        return servicePersistencePort.findAllByAllowedForOrganizerTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public com.trackfindergarage.backend.domain.model.Service getServiceById(Long id) {
        return servicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
    }

    @Override
    public com.trackfindergarage.backend.domain.model.Service enableService(Long id) {
        com.trackfindergarage.backend.domain.model.Service existingService = findServiceOrThrow(id);
        existingService.setEnabled(true);
        return servicePersistencePort.save(existingService);
    }

    @Override
    public com.trackfindergarage.backend.domain.model.Service disableService(Long id) {
        com.trackfindergarage.backend.domain.model.Service existingService = findServiceOrThrow(id);
        existingService.setEnabled(false);
        return servicePersistencePort.save(existingService);
    }

    //Función privada que hace lo mismo que getServiceById. Los métodos con proxy de Spring no deben ser llamados desde dentro del propio bean.
    private com.trackfindergarage.backend.domain.model.Service findServiceOrThrow(Long id) {
        return servicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
    }
}
