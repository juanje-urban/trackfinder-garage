package com.trackfindergarage.backend.application.port.in;

/**
 * Datos internos para actualizar el perfil del organizador autenticado.
 */
public record UpdateCurrentOrganizerProfileCommand(String name,
                                                   String surname,
                                                   String email,
                                                   String address,
                                                   String phone,
                                                   String legalName,
                                                   String cif,
                                                   String rawPassword) {
}
