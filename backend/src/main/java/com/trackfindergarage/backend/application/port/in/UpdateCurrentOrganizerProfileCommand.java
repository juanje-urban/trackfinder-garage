package com.trackfindergarage.backend.application.port.in;

public record UpdateCurrentOrganizerProfileCommand(String name,
                                                   String surname,
                                                   String email,
                                                   String address,
                                                   String phone,
                                                   String legalName,
                                                   String cif,
                                                   String rawPassword) {
}
