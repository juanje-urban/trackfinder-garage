package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Service;

import java.util.List;

public interface ServiceUseCase {

    Service createService(Service service);

    Service updateService(Long id, Service service);

    List<Service> getAllServices();

    Service enableService(Long id);

    Service disableService(Long id);
}
