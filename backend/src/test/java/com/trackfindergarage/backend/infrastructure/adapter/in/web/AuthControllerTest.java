package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.AuthRegistrationCommand;
import com.trackfindergarage.backend.application.port.in.AuthUseCase;
import com.trackfindergarage.backend.application.port.in.OrganizerRegistrationCommand;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthLoginRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthRegisterRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AuthUseCase authUseCase = mock(AuthUseCase.class);
    private final AuthController authController = new AuthController(authUseCase);

    @Test
    void loginDelegatesToUseCase() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("driver@example.com");
        request.setPassword("secret");

        AuthResponse expected = response(3L, "driver", "driver@example.com");
        when(authUseCase.login("driver@example.com", "secret")).thenReturn(expected);

        AuthResponse response = authController.login(request);

        assertEquals(expected.getUserId(), response.getUserId());
        assertEquals(expected.getDisplayName(), response.getDisplayName());
        verify(authUseCase).login("driver@example.com", "secret");
    }

    @Test
    void getCurrentSessionDelegatesToUseCase() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("driver@example.com", "secret");
        AuthResponse expected = response(3L, "driver", "driver@example.com");
        when(authUseCase.getCurrentSession("driver@example.com")).thenReturn(expected);

        AuthResponse response = authController.getCurrentSession(authentication);

        assertEquals(expected.getUserId(), response.getUserId());
        verify(authUseCase).getCurrentSession("driver@example.com");
    }

    @Test
    void registerDelegatesToUseCase() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setDisplayName("latebraker");
        request.setEmail("driver@example.com");
        request.setPassword("secret");
        request.setName("Laura");
        request.setSurname("Sanz");
        request.setAddress("Calle Box 27");
        request.setPhone("666555444");

        AuthResponse expected = response(5L, "latebraker", "driver@example.com");
        AuthRegistrationCommand registrationCommand = new AuthRegistrationCommand(
                "latebraker",
                "driver@example.com",
                "secret",
                "Laura",
                "Sanz",
                "Calle Box 27",
                "666555444"
        );
        when(authUseCase.register(registrationCommand)).thenReturn(expected);

        AuthResponse response = authController.register(request);

        assertEquals(expected.getUserId(), response.getUserId());
        assertEquals(expected.getEmail(), response.getEmail());
        verify(authUseCase).register(registrationCommand);
    }

    @Test
    void registerOrganizerDelegatesToUseCase() {
        CreateOrganizerRequest request = new CreateOrganizerRequest();
        request.setDisplayName("tracklimits");
        request.setEmail("tracklimits@example.com");
        request.setPassword("secret");
        request.setName("Laura");
        request.setSurname("Sanz");
        request.setAddress("Calle Box 27");
        request.setPhone("666555444");
        request.setLegalName("Track Limits Iberia S.L.");
        request.setCif("B12345678");

        AuthResponse expected = response(8L, "tracklimits", "tracklimits@example.com");
        expected.setRoleName("ORGANIZER");
        OrganizerRegistrationCommand organizerRegistrationCommand = new OrganizerRegistrationCommand(
                new AuthRegistrationCommand(
                        "tracklimits",
                        "tracklimits@example.com",
                        "secret",
                        "Laura",
                        "Sanz",
                        "Calle Box 27",
                        "666555444"
                ),
                "Track Limits Iberia S.L.",
                "B12345678"
        );
        when(authUseCase.registerOrganizer(organizerRegistrationCommand)).thenReturn(expected);

        AuthResponse response = authController.registerOrganizer(request);

        assertEquals(expected.getUserId(), response.getUserId());
        assertEquals(expected.getRoleName(), response.getRoleName());
        verify(authUseCase).registerOrganizer(organizerRegistrationCommand);
    }

    private AuthResponse response(Long id, String displayName, String email) {
        AuthResponse response = new AuthResponse();
        response.setUserId(id);
        response.setDisplayName(displayName);
        response.setEmail(email);
        response.setRoleName("USER");
        return response;
    }
}
