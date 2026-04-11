package com.trackfindergarage.backend.application.port.in;

public record OrganizerRegistrationCommand(AuthRegistrationCommand authRegistration,
                                           String legalName,
                                           String cif) {
}
