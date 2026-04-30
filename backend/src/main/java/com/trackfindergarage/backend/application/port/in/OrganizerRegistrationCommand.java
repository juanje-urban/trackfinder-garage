package com.trackfindergarage.backend.application.port.in;

/**
 * Datos internos usados para registrar un organizador junto con su usuario asociado.
 */
public record OrganizerRegistrationCommand(AuthRegistrationCommand authRegistration,
                                           String legalName,
                                           String cif) {
}
