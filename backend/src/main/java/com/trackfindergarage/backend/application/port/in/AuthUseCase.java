package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;

/**
 * Define los casos de uso de autenticación expuestos por la aplicación.
 */
public interface AuthUseCase {

    /**
     * Autentica a un usuario a partir de sus credenciales.
     *
     * @param email email proporcionado en el login
     * @param rawPassword contraseña en texto plano
     * @return respuesta de sesión para el cliente
     */
    AuthResponse login(String email, String rawPassword);

    /**
     * Obtiene la sesión actual del usuario autenticado.
     *
     * @param authenticatedEmail email resuelto por la capa de seguridad
     * @return respuesta de sesión para el cliente
     */
    AuthResponse getCurrentSession(String authenticatedEmail);

    /**
     * Registra un nuevo usuario final.
     *
     * @param command datos necesarios para crear el usuario
     * @return respuesta de sesión del usuario creado
     */
    AuthResponse register(AuthRegistrationCommand command);

    /**
     * Registra un nuevo organizador y su usuario asociado.
     *
     * @param command datos de alta del organizador
     * @return respuesta de sesión del usuario creado
     */
    AuthResponse registerOrganizer(OrganizerRegistrationCommand command);
}
