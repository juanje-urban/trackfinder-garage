package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.OrganizerServiceUseCase;
import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrganizerServiceService implements OrganizerServiceUseCase {

    private final OrganizerServicePersistencePort organizerServicePersistencePort;
    private final OrganizerPersistencePort organizerPersistencePort;
    private final ServicePersistencePort servicePersistencePort;

    public OrganizerServiceService(OrganizerServicePersistencePort organizerServicePersistencePort,
                                   OrganizerPersistencePort organizerPersistencePort,
                                   ServicePersistencePort servicePersistencePort) {
        this.organizerServicePersistencePort = organizerServicePersistencePort;
        this.organizerPersistencePort = organizerPersistencePort;
        this.servicePersistencePort = servicePersistencePort;
    }

    @Override
    public OrganizerService createOrganizerService(OrganizerService organizerService) {
        Long organizerId = extractOrganizerId(organizerService);
        Long serviceId = extractServiceId(organizerService);

        organizerServicePersistencePort.findByOrganizerIdUserAndServiceId(organizerId, serviceId)
                .ifPresent(existingAssignment -> {
                    throw new DuplicateResourceException(
                            "Organizer service already exists for organizer id " + organizerId
                                    + " and service id " + serviceId
                    );
                });

        Organizer organizer = organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));

        com.trackfindergarage.backend.domain.model.Service service = servicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        validateServiceAllowedForOrganizer(service);

        organizerService.setOrganizer(organizer);
        organizerService.setService(service);

        return organizerServicePersistencePort.save(organizerService);
    }

    @Override
    public void deleteOrganizerService(Long id) {
        OrganizerService organizerService = findOrganizerServiceOrThrow(id);
        organizerServicePersistencePort.delete(organizerService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerService> getAllOrganizerServices() {
        return organizerServicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizerService getOrganizerServiceById(Long id) {
        return organizerServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer service not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerService> getOrganizerServicesByOrganizerId(Long organizerId) {
        organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));

        return organizerServicePersistencePort.findByOrganizerIdUser(organizerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerService> getOrganizerServicesByServiceId(Long serviceId) {
        servicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        return organizerServicePersistencePort.findByServiceId(serviceId);
    }

    private Long extractOrganizerId(OrganizerService organizerService) {
        if (organizerService.getOrganizer() == null || organizerService.getOrganizer().getIdUser() == null) {
            throw new IllegalArgumentException("Organizer id is required");
        }
        return organizerService.getOrganizer().getIdUser();
    }

    private Long extractServiceId(OrganizerService organizerService) {
        if (organizerService.getService() == null || organizerService.getService().getId() == null) {
            throw new IllegalArgumentException("Service id is required");
        }
        return organizerService.getService().getId();
    }

    private void validateServiceAllowedForOrganizer(com.trackfindergarage.backend.domain.model.Service service) {
        if (!Boolean.TRUE.equals(service.getAllowedForOrganizer())) {
            throw new IllegalArgumentException(
                    "Service with id " + service.getId() + " is not allowed for organizers"
            );
        }
    }

    private OrganizerService findOrganizerServiceOrThrow(Long id) {
        return organizerServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer service not found with id: " + id));
    }
}
