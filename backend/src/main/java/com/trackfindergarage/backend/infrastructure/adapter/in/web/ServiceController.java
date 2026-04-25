package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.ServiceUseCase;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.ServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.ServiceWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints administrativos del catálogo de servicios.
 */
@RestController
@RequestMapping("/services")
@PreAuthorize("hasRole('ADMIN')")
public class ServiceController extends AbstractWebController {

    private final ServiceUseCase serviceUseCase;
    private final ServiceWebMapper serviceWebMapper;

    public ServiceController(ServiceUseCase serviceUseCase, ServiceWebMapper serviceWebMapper) {
        this.serviceUseCase = serviceUseCase;
        this.serviceWebMapper = serviceWebMapper;
    }

    /**
     * Crea un nuevo servicio de catalogo.
     *
     * @param request datos del servicio
     * @return servicio creado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(@Valid @RequestBody CreateServiceRequest request) {
        return serviceWebMapper.toResponse(serviceUseCase.createService(serviceWebMapper.toDomain(request)));
    }

    /**
     * Actualiza un servicio existente.
     *
     * @param id identificador del servicio
     * @param request nuevos datos del servicio
     * @return servicio actualizado
     */
    @PutMapping("/{id}")
    public ServiceResponse updateService(@PathVariable Long id,
                                         @Valid @RequestBody UpdateServiceRequest request) {
        Service serviceToUpdate = new Service();
        serviceWebMapper.updateDomain(serviceToUpdate, request);
        return serviceWebMapper.toResponse(serviceUseCase.updateService(id, serviceToUpdate));
    }

    /**
     * Recupera todos los servicios del catálogo.
     *
     * @return listado de servicios
     */
    @GetMapping
    public List<ServiceResponse> getAllServices() {
        return mapResponses(serviceUseCase.getAllServices(), serviceWebMapper::toResponse);
    }

    /**
     * Habilita un servicio del catalogo.
     *
     * @param id identificador del servicio
     * @return servicio habilitado
     */
    @PatchMapping("/{id}/enable")
    public ServiceResponse enableService(@PathVariable Long id) {
        return serviceWebMapper.toResponse(serviceUseCase.enableService(id));
    }

    /**
     * Deshabilita un servicio.
     *
     * @param id identificador del servicio
     * @return servicio deshabilitado
     */
    @PatchMapping("/{id}/disable")
    public ServiceResponse disableService(@PathVariable Long id) {
        return serviceWebMapper.toResponse(serviceUseCase.disableService(id));
    }
}
