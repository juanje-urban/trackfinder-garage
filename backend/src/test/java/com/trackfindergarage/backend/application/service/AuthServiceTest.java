package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.InvalidCredentialsException;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private OrganizerUseCase organizerUseCase;

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsAuthResponseForValidCredentials() {
        User user = enabledUser(7L, "apexhunter", "driver@example.com");

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        AuthResponse response = authService.login(" DRIVER@example.com ", "secret");

        assertEquals(7L, response.getUserId());
        assertEquals("apexhunter", response.getDisplayName());
        assertEquals("driver@example.com", response.getEmail());
        assertEquals("USER", response.getRoleName());
        assertEquals(expectedAuthorizationHeader("driver@example.com", "secret"), response.getAuthorizationHeader());
    }

    @Test
    void loginThrowsForWrongPassword() {
        User user = enabledUser(7L, "apexhunter", "driver@example.com");

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-pass", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login("driver@example.com", "bad-pass"));
    }

    @Test
    void registerCreatesUserWithFullProfileAndLogsUserIn() {
        when(userUseCase.createUser(any(User.class), eq("secret"))).thenAnswer(invocation -> {
            User createdUser = invocation.getArgument(0);
            createdUser.setId(11L);
            Role role = new Role();
            role.setRoleName("USER");
            createdUser.setRole(role);
            return createdUser;
        });

        AuthResponse response = authService.register(
                "latebraker_88",
                "latebraker@example.com",
                "secret",
                "Laura",
                "Sanz",
                "Calle Box 27",
                "666555444"
        );

        assertEquals(11L, response.getUserId());
        assertEquals("latebraker_88", response.getDisplayName());
        assertEquals("latebraker@example.com", response.getEmail());
        assertEquals("USER", response.getRoleName());
        assertNotNull(response.getAuthorizationHeader());
        verify(userUseCase).createUser(any(User.class), eq("secret"));
    }

    @Test
    void registerTrimsProfileFieldsBeforeCreatingUser() {
        when(userUseCase.createUser(any(User.class), eq("secret")))
                .thenAnswer(invocation -> {
                    User createdUser = invocation.getArgument(0);
                    Role role = new Role();
                    role.setRoleName("USER");
                    createdUser.setRole(role);
                    return createdUser;
                });

        AuthResponse response = authService.register(
                "  latebraker_88  ",
                "latebraker@example.com",
                "secret",
                "  Laura  ",
                "  Sanz  ",
                "  Calle Box 27  ",
                " 666555444 "
        );

        assertEquals("latebraker_88", response.getDisplayName());
    }

    @Test
    void loginThrowsForDisabledUsers() {
        User user = enabledUser(7L, "apexhunter", "driver@example.com");
        user.setEnabled(false);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> authService.login("driver@example.com", "secret"));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void registerOrganizerReturnsOrganizerRole() {
        when(organizerUseCase.createOrganizer(any(Organizer.class), eq("secret"))).thenAnswer(invocation -> {
            Organizer createdOrganizer = invocation.getArgument(0);
            createdOrganizer.setIdUser(20L);
            User user = createdOrganizer.getUser();
            user.setId(20L);
            Role role = new Role();
            role.setRoleName("ORGANIZER");
            user.setRole(role);
            return createdOrganizer;
        });

        AuthResponse response = authService.registerOrganizer(
                "tracklimits",
                "tracklimits@example.com",
                "secret",
                "Laura",
                "Sanz",
                "Calle Box 27",
                "666555444",
                "Track Limits Iberia S.L.",
                "B12345678"
        );

        assertEquals(20L, response.getUserId());
        assertEquals("tracklimits", response.getDisplayName());
        assertEquals("ORGANIZER", response.getRoleName());
    }

    private User enabledUser(Long id, String displayName, String email) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setEnabled(true);
        Role role = new Role();
        role.setRoleName("USER");
        user.setRole(role);
        return user;
    }

    private String expectedAuthorizationHeader(String email, String rawPassword) {
        String token = email + ":" + rawPassword;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }
}
