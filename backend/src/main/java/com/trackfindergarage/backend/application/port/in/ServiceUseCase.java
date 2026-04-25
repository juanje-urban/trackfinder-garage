package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Service;

import java.util.List;

/**
 * Define los casos de uso para administrar el catálogo de servicios.
 */
public interface ServiceUseCase {

    /**
     * Crea un nuevo servicio de catalogo.
     *
     * @param service datos del servicio
     * @return servicio persistido
     */
    Service createService(Service service);

    /**
     * Actualiza un servicio existente.
     *
     * @param id identificador del servicio
     * @param service nuevos datos del servicio
     * @return servicio actualizado
     */
    Service updateService(Long id, Service service);

    /**
     * Recupera todos los servicios del catálogo.
     *
     * @return listado de servicios
     */
    List<Service> getAllServices();

    /**
     * Habilita un servicio del catalogo.
     *
     * @param id identificador del servicio
     * @return servicio habilitado
     */
    Service enableService(Long id);

    /**
     * Deshabilita un servicio del catalogo.
     *
     * @param id identificador del servicio
     * @return servicio deshabilitado
     */
    Service disableService(Long id);
}
