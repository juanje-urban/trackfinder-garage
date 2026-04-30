package com.trackfindergarage.backend.application.port.in;

/**
 * Datos internos necesarios para registrar las credenciales y el perfil base de un usuario.
 */
public record AuthRegistrationCommand(String displayName,
                                      String email,
                                      String rawPassword,
                                      String name,
                                      String surname,
                                      String address,
                                      String phone) {
}
