package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.OrganizerServiceUseCase;
import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class OrganizerServiceService implements OrganizerServiceUseCase {

    private static final String ORGANIZER_NOT_FOUND_WITH_ID = "Organizador no encontrado con id: ";
    private static final String SERVICE_NOT_FOUND_WITH_ID = "Servicio no encontrado con id: ";
    private static final String ORGANIZER_SERVICE_NOT_FOUND_WITH_ID = "Servicio de organizador no encontrado con id: ";
    private static final String ORGANIZER_ID_REQUIRED = "El id del organizador es obligatorio";
    private static final String SERVICE_ID_REQUIRED = "El id del servicio es obligatorio";
    private static final String ORGANIZER_SERVICE_ALREADY_EXISTS =
            "Ya existe un servicio de organizador para el organizador con id %d y el servicio con id %d";
    private static final String SERVICE_NOT_ALLOWED_FOR_ORGANIZERS =
            "El servicio con id %d no está permitido para organizadores";

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

        OrganizerService existingAssignment = organizerServicePersistencePort
                .findByOrganizerIdUserAndServiceId(organizerId, serviceId)
                .orElse(null);

        if (existingAssignment != null) {
            if (Boolean.FALSE.equals(existingAssignment.getEnabled())) {
                existingAssignment.setEnabled(true);
                return organizerServicePersistencePort.save(existingAssignment);
            }

            throw new DuplicateResourceException(
                    ORGANIZER_SERVICE_ALREADY_EXISTS.formatted(organizerId, serviceId)
            );
        }

        Organizer organizer = organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + organizerId));

        Service service = servicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVICE_NOT_FOUND_WITH_ID + serviceId));

        validateServiceAllowedForOrganizer(service);

        organizerService.setOrganizer(organizer);
        organizerService.setService(service);
        organizerService.setEnabled(true);

        return organizerServicePersistencePort.save(organizerService);
    }

    @Override
    public void deleteOrganizerService(Long id) {
        OrganizerService organizerService = findOrganizerServiceOrThrow(id);
        organizerService.setEnabled(false);
        organizerServicePersistencePort.save(organizerService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerService> getAllOrganizerServices() {
        return organizerServicePersistencePort.findAll().stream()
                .filter(this::isCatalogActive)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizerService getOrganizerServiceById(Long id) {
        return organizerServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_SERVICE_NOT_FOUND_WITH_ID + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerService> getOrganizerServicesByOrganizerId(Long organizerId) {
        organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + organizerId));

        return organizerServicePersistencePort.findByOrganizerIdUser(organizerId).stream()
                .filter(this::isCatalogActive)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizerService> getOrganizerServicesByServiceId(Long serviceId) {
        servicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVICE_NOT_FOUND_WITH_ID + serviceId));

        return organizerServicePersistencePort.findByServiceId(serviceId).stream()
                .filter(this::isCatalogActive)
                .toList();
    }

    private Long extractOrganizerId(OrganizerService organizerService) {
        if (organizerService.getOrganizer() == null || organizerService.getOrganizer().getIdUser() == null) {
            throw new IllegalArgumentException(ORGANIZER_ID_REQUIRED);
        }
        return organizerService.getOrganizer().getIdUser();
    }

    private Long extractServiceId(OrganizerService organizerService) {
        if (organizerService.getService() == null || organizerService.getService().getId() == null) {
            throw new IllegalArgumentException(SERVICE_ID_REQUIRED);
        }
        return organizerService.getService().getId();
    }

    private void validateServiceAllowedForOrganizer(Service service) {
        if (!Boolean.TRUE.equals(service.getAllowedForOrganizer())) {
            throw new IllegalArgumentException(SERVICE_NOT_ALLOWED_FOR_ORGANIZERS.formatted(service.getId()));
        }
    }

    private boolean isCatalogActive(OrganizerService organizerService) {
        return !Boolean.FALSE.equals(organizerService.getEnabled());
    }

    private OrganizerService findOrganizerServiceOrThrow(Long id) {
        return organizerServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_SERVICE_NOT_FOUND_WITH_ID + id));
    }
}
