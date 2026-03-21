package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Service;

import java.util.List;

public interface ServiceUseCase {

    Service createService(Service service);

    Service updateService(Long id, Service service);

    void deleteService(Long id);

    List<Service> getAllServices();

    List<Service> getAllServicesAllowedForTrack();

    List<Service> getAllServicesAllowedForOrganizer();

    Service getServiceById(Long id);

    Service enableService(Long id);

    Service disableService(Long id);
}
