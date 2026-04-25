package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.ServiceUseCase;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementa la lógica de administración del catalogo de servicios.
 *
 * <p>Controla la unicidad del nombre, mantiene el estado habilitado del servicio y expone las operaciones
 * básicas de alta, modificación, etc.</p>
 */
@org.springframework.stereotype.Service
@Transactional
public class ServiceService implements ServiceUseCase {

    private static final String SERVICE_NOT_FOUND_WITH_ID = "Servicio no encontrado con id: ";
    private static final String SERVICE_NAME_ALREADY_EXISTS = "Ya existe un servicio con el nombre '%s'";

    private final ServicePersistencePort servicePersistencePort;

    public ServiceService(ServicePersistencePort servicePersistencePort) {
        this.servicePersistencePort = servicePersistencePort;
    }

    /**
     * Crea un nuevo servicio de catalogo.
     *
     * @param service datos del servicio
     * @return servicio persistido
     */
    @Override
    public Service createService(Service service) {
        servicePersistencePort.findByName(service.getName())
                .ifPresent(existingService -> {
                    throw new DuplicateResourceException(SERVICE_NAME_ALREADY_EXISTS.formatted(service.getName()));
                });

        service.setEnabled(true);
        return servicePersistencePort.save(service);
    }

    /**
     * Actualiza un servicio existente.
     *
     * @param id identificador del servicio
     * @param service nuevos datos del servicio
     * @return servicio actualizado
     */
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

    /**
     * Recupera el catálogo completo de servicios.
     *
     * @return listado de servicios
     */
    @Override
    @Transactional(readOnly = true)
    public List<Service> getAllServices() {
        return servicePersistencePort.findAll();
    }

    /**
     * Habilita un servicio.
     *
     * @param id identificador del servicio
     * @return servicio habilitado
     */
    @Override
    public Service enableService(Long id) {
        Service existingService = findServiceOrThrow(id);
        existingService.setEnabled(true);
        return servicePersistencePort.save(existingService);
    }

    /**
     * Deshabilita un servicio existente.
     *
     * @param id identificador del servicio
     * @return servicio deshabilitado
     */
    @Override
    public Service disableService(Long id) {
        Service existingService = findServiceOrThrow(id);
        existingService.setEnabled(false);
        return servicePersistencePort.save(existingService);
    }

    private Service findServiceOrThrow(Long id) {
        return servicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SERVICE_NOT_FOUND_WITH_ID + id));
    }
}
