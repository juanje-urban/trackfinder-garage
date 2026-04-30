package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.OrganizerService;

import java.util.List;

/**
 * Define las operaciones de aplicación para gestionar los servicios que ofrece un organizador.
 */
public interface OrganizerServiceUseCase {

    /**
     * Crea o reactiva una asignación de servicio para un organizador.
     *
     * @param organizerService relación entre organizador y servicio
     * @return asignación persistida
     */
    OrganizerService createOrganizerService(OrganizerService organizerService);

    /**
     * Desactiva una asignación de servicio del organizador.
     *
     * @param id identificador de la asignación
     */
    void deleteOrganizerService(Long id);

    /**
     * Recupera una asignación concreta.
     *
     * @param id identificador de la asignación
     * @return asignación encontrada
     */
    OrganizerService getOrganizerServiceById(Long id);

    /**
     * Recupera los servicios activos de un organizador.
     *
     * @param organizerId identificador del organizador
     * @return listado de asignaciones activas
     */
    List<OrganizerService> getOrganizerServicesByOrganizerId(Long organizerId);
}
