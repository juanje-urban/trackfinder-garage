package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.OrganizerServicePersistencePort;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrganizerServicePersistenceAdapter implements OrganizerServicePersistencePort {

    private final SpringDataOrganizerServiceRepository organizerServiceRepository;

    public OrganizerServicePersistenceAdapter(SpringDataOrganizerServiceRepository organizerServiceRepository) {
        this.organizerServiceRepository = organizerServiceRepository;
    }

    @Override
    public OrganizerService save(OrganizerService organizerService) {
        return organizerServiceRepository.save(organizerService);
    }

    @Override
    public Optional<OrganizerService> findById(Long id) {
        return organizerServiceRepository.findById(id);
    }

    @Override
    public List<OrganizerService> findAll() {
        return organizerServiceRepository.findAll();
    }

    @Override
    public List<OrganizerService> findByOrganizerIdUser(Long organizerId) {
        return organizerServiceRepository.findByOrganizerIdUser(organizerId);
    }

    @Override
    public List<OrganizerService> findByServiceId(Long serviceId) {
        return organizerServiceRepository.findByServiceId(serviceId);
    }

    @Override
    public Optional<OrganizerService> findByOrganizerIdUserAndServiceId(Long organizerId, Long serviceId) {
        return organizerServiceRepository.findByOrganizerIdUserAndServiceId(organizerId, serviceId);
    }

    @Override
    public void delete(OrganizerService organizerService) {
        organizerServiceRepository.delete(organizerService);
    }
}
