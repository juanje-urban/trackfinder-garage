package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.OrganizerService;

import java.util.List;

public interface OrganizerServiceUseCase {

    OrganizerService createOrganizerService(OrganizerService organizerService);

    void deleteOrganizerService(Long id);

    OrganizerService getOrganizerServiceById(Long id);

    List<OrganizerService> getOrganizerServicesByOrganizerId(Long organizerId);
}
