package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;

public interface AuthUseCase {

    AuthResponse login(String email, String rawPassword);

    AuthResponse register(String displayName,
                          String email,
                          String rawPassword,
                          String name,
                          String surname,
                          String address,
                          String phone);

    AuthResponse registerOrganizer(String displayName,
                                   String email,
                                   String rawPassword,
                                   String name,
                                   String surname,
                                   String address,
                                   String phone,
                                   String legalName,
                                   String cif);
}
