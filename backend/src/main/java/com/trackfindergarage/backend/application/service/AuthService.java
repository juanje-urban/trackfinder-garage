package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.AuthUseCase;
import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.InvalidCredentialsException;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Service
@Transactional
public class AuthService implements AuthUseCase {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Correo o contrasena incorrectos";

    private final UserUseCase userUseCase;
    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserUseCase userUseCase,
                       UserPersistencePort userPersistencePort,
                       PasswordEncoder passwordEncoder) {
        this.userUseCase = userUseCase;
        this.userPersistencePort = userPersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);

        User user = userPersistencePort.findByEmail(normalizedEmail)
                .filter(existingUser -> Boolean.TRUE.equals(existingUser.getEnabled()))
                .filter(existingUser -> passwordEncoder.matches(rawPassword, existingUser.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        return buildAuthResponse(user, normalizedEmail, rawPassword);
    }

    @Override
    public AuthResponse register(String displayName,
                                 String email,
                                 String rawPassword,
                                 String name,
                                 String surname,
                                 String address,
                                 String phone) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedDisplayName = normalizeText(displayName);

        User user = new User();
        user.setDisplayName(normalizedDisplayName);
        user.setEmail(normalizedEmail);
        user.setName(normalizeText(name));
        user.setSurname(normalizeText(surname));
        user.setAddress(normalizeText(address));
        user.setPhone(normalizeText(phone));

        User createdUser = userUseCase.createUser(user, rawPassword);
        return buildAuthResponse(createdUser, normalizedEmail, rawPassword);
    }

    private AuthResponse buildAuthResponse(User user, String normalizedEmail, String rawPassword) {
        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        response.setAuthorizationHeader(buildAuthorizationHeader(normalizedEmail, rawPassword));
        return response;
    }

    private String buildAuthorizationHeader(String email, String rawPassword) {
        String token = email + ":" + rawPassword;
        String encodedToken = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedToken;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value.trim();
    }
}
