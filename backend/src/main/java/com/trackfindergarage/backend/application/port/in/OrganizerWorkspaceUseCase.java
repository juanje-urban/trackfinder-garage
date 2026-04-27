package com.trackfindergarage.backend.application.port.in;

/**
 * Define los casos de uso del espacio de trabajo del organizador.
 */
public interface OrganizerWorkspaceUseCase {

    /**
     * Recupera la instantánea completa del workspace del organizador autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return estado completo del workspace
     */
    OrganizerWorkspaceSnapshot getWorkspace(String authenticatedEmail);

    /**
     * Añade un servicio del catálogo al organizador.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param serviceId identificador del servicio a añadir
     * @return workspace actualizado
     */
    OrganizerWorkspaceSnapshot addOrganizerService(String authenticatedEmail, Long serviceId);

    /**
     * Elimina un servicio propio del organizador.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param organizerServiceId identificador de la asignación a eliminar
     * @return workspace actualizado
     */
    OrganizerWorkspaceSnapshot removeOrganizerService(String authenticatedEmail, Long organizerServiceId);

    /**
     * Crea un evento nuevo dentro del workspace del organizador.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param draft borrador con los datos del evento
     * @return workspace actualizado
     */
    OrganizerWorkspaceSnapshot createEvent(String authenticatedEmail, OrganizerEventDraft draft);

    /**
     * Actualiza un evento existente del organizador autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param eventId identificador del evento a modificar
     * @param draft borrador con los nuevos datos del evento
     * @return workspace actualizado
     */
    OrganizerWorkspaceSnapshot updateEvent(String authenticatedEmail, Long eventId, OrganizerEventDraft draft);

    /**
     * Elimina un evento del organizador autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param eventId identificador del evento a eliminar
     * @return workspace actualizado
     */
    OrganizerWorkspaceSnapshot deleteEvent(String authenticatedEmail, Long eventId);
}
