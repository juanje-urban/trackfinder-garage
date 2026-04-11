package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;

public interface AuthUseCase {

    AuthResponse login(String email, String rawPassword);

    AuthResponse getCurrentSession(String authenticatedEmail, String authorizationHeader);

    AuthResponse register(AuthRegistrationCommand command);

    AuthResponse registerOrganizer(OrganizerRegistrationCommand command);
}
