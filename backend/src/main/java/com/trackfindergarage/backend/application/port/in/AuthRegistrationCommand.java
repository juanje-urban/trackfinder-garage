package com.trackfindergarage.backend.application.port.in;

public record AuthRegistrationCommand(String displayName,
                                      String email,
                                      String rawPassword,
                                      String name,
                                      String surname,
                                      String address,
                                      String phone) {
}
