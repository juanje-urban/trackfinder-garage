package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.ServiceUseCase;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class ServiceService implements ServiceUseCase {

    private static final String SERVICE_NOT_FOUND_WITH_ID = "Service not found with id: ";
    private static final String SERVICE_NAME_ALREADY_EXISTS = "Service with name '%s' already exists";

    private final ServicePersistencePort servicePersistencePort;

    public ServiceService(ServicePersistencePort servicePersistencePort) {
        this.servicePersistencePort = servicePersistencePort;
    }

    @Override
    public Service createService(Service service) {
        servicePersistencePort.findByName(service.getName())
                .ifPresent(existingService -> {
                    throw new DuplicateResourceException(SERVICE_NAME_ALREADY_EXISTS.formatted(service.getName()));
                });

        service.setEnabled(true);
        return servicePersistencePort.save(service);
    }

    @Override
    public Service updateService(Long id, Service service) {
        Service existingService = findServiceOrThrow(id);

        servicePersistencePort.findByName(service.getName())
                .ifPresent(foundService -> {
                    if (!foundService.getId().equals(id)) {
                        throw new DuplicateResourceException(SERVICE_NAME_ALREADY_EXISTS.formatted(service.getName()));
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
        Service existingService = findServiceOrThrow(id);
        servicePersistencePort.delete(existingService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> getAllServices() {
        return servicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> getAllServicesAllowedForTrack() {
        return servicePersistencePort.findAllByAllowedForTrackTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> getAllServicesAllowedForOrganizer() {
        return servicePersistencePort.findAllByAllowedForOrganizerTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Service getServiceById(Long id) {
        return servicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SERVICE_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public Service enableService(Long id) {
        Service existingService = findServiceOrThrow(id);
        existingService.setEnabled(true);
        return servicePersistencePort.save(existingService);
    }

    @Override
    public Service disableService(Long id) {
        Service existingService = findServiceOrThrow(id);
        existingService.setEnabled(false);
        return servicePersistencePort.save(existingService);
    }

    //Función privada que hace lo mismo que getServiceById. Los métodos con proxy de Spring no deben ser llamados desde dentro del propio bean.
    private Service findServiceOrThrow(Long id) {
        return servicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SERVICE_NOT_FOUND_WITH_ID + id));
    }
}
