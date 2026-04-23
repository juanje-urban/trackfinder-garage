package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.User;

import java.util.List;

/**
 * Define las operaciones de aplicación relacionadas con usuarios y administradores.
 */
public interface UserUseCase {

    /**
     * Crea un nuevo usuario.
     *
     * @param user datos del usuario a crear
     * @param rawPassword contraseña en texto plano a codificar
     * @return usuario persistido
     */
    User createUser(User user, String rawPassword);

    /**
     * Obtiene el usuario asociado al email autenticado en la petición actual.
     *
     * @param authenticatedEmail email resuelto por la capa de seguridad
     * @return usuario autenticado
     */
    User getCurrentUser(String authenticatedEmail);

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param authenticatedEmail email del usuario autenticado
     * @param name nombre actualizado
     * @param surname apellidos actualizados
     * @param email email actualizado
     * @param address dirección actualizada
     * @param phone teléfono actualizado
     * @param rawPassword nueva contraseña opcional
     * @return usuario actualizado
     */
    User updateCurrentUserProfile(String authenticatedEmail,
                                  String name,
                                  String surname,
                                  String email,
                                  String address,
                                  String phone,
                                  String rawPassword);

    /**
     * Lista todos los usuarios gestionados por la plataforma.
     *
     * @return listado completo de usuarios
     */
    List<User> getAllUsers();

    /**
     * Habilita una cuenta de usuario.
     *
     * @param id identificador del usuario
     * @return usuario habilitado
     */
    User enableUser(Long id);

    /**
     * Deshabilita una cuenta de usuario.
     *
     * @param id identificador del usuario
     * @return usuario deshabilitado
     */
    User disableUser(Long id);
}
