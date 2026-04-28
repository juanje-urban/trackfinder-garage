package com.trackfindergarage.backend.application.port.in;

/**
 * Define los casos de uso relacionados con la información pública de los usuarios.
 */
public interface PublicProfileUseCase {

    /**
     * Recupera el perfil público resumido de un usuario a partir de su alias.
     *
     * @param displayName alias público del usuario
     * @return vista pública del perfil
     */
    PublicUserProfileView getPublicUserProfile(String displayName);
}
