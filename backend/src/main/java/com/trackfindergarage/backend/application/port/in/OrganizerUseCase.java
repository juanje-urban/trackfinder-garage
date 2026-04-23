package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Organizer;

import java.util.List;

/**
 * Define las operaciones de aplicación relacionadas con organizadores.
 */
public interface OrganizerUseCase {

    /**
     * Crea un nuevo organizador y su usuario asociado.
     *
     * @param organizer datos del organizador a persistir
     * @param rawPassword contraseña en texto plano del usuario asociado
     * @return organizador persistido
     */
    Organizer createOrganizer(Organizer organizer, String rawPassword);

    /**
     * Obtiene el organizador vinculado al email autenticado.
     *
     * @param authenticatedEmail email resuelto por la capa de seguridad
     * @return organizador autenticado
     */
    Organizer getCurrentOrganizer(String authenticatedEmail);

    /**
     * Actualiza el perfil del organizador autenticado.
     *
     * @param authenticatedEmail email del usuario autenticado
     * @param profileCommand comando con los nuevos datos del perfil
     * @return organizador actualizado
     */
    Organizer updateCurrentOrganizerProfile(String authenticatedEmail,
                                            UpdateCurrentOrganizerProfileCommand profileCommand);

    /**
     * Elimina un organizador y su usuario asociado.
     *
     * @param id identificador del organizador
     */
    void deleteOrganizer(Long id);

    /**
     * Lista todos los organizadores registrados.
     *
     * @return listado completo de organizadores
     */
    List<Organizer> getAllOrganizers();

    /**
     * Habilita una cuenta de organizador.
     *
     * @param id identificador del organizador
     * @return organizador habilitado
     */
    Organizer enableOrganizer(Long id);
}
