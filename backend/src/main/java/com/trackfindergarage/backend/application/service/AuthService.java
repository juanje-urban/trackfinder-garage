package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.AuthRegistrationCommand;
import com.trackfindergarage.backend.application.port.in.AuthUseCase;
import com.trackfindergarage.backend.application.port.in.OrganizerRegistrationCommand;
import com.trackfindergarage.backend.application.port.in.OrganizerUseCase;
import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.InvalidCredentialsException;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Implementa la lógica de autenticación y registro de usuarios.
 *
 * <p>Este servicio centraliza el login, la recuperación de la sesión actual y los flujos de alta tanto de usuarios
 * normales ({@code USER}) como de organizadores ({@code ORGANIZER}). Además, normaliza los datos de entrada y construye
 * la respuesta de autenticación que consume el frontend.</p>
 */
@Service
@Transactional
public class AuthService implements AuthUseCase {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Correo o contraseña incorrectos";

    private final OrganizerUseCase organizerUseCase;
    private final UserUseCase userUseCase;
    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public AuthService(OrganizerUseCase organizerUseCase,
                       UserUseCase userUseCase,
                       UserPersistencePort userPersistencePort,
                       PasswordEncoder passwordEncoder) {
        this.organizerUseCase = organizerUseCase;
        this.userUseCase = userUseCase;
        this.userPersistencePort = userPersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Comprueba las credenciales de un usuario activo y devuelve su sesión.
     *
     * @param email email introducido en el login
     * @param rawPassword contraseña en texto plano introducida por el usuario
     * @return datos básicos de sesión del usuario autenticado
     * @throws InvalidCredentialsException si el usuario no existe, esta deshabilitado o la contraseña no coincide
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        User user = loadActiveUserByEmail(normalizedEmail);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        return buildAuthResponse(user);
    }

    /**
     * Reconstruye la sesión actual a partir del usuario autenticado por Spring Security.
     *
     * @param authenticatedEmail email del usuario autenticado en la petición
     * @return estado actual de la sesión
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentSession(String authenticatedEmail) {
        return buildAuthResponse(loadActiveUserByEmail(normalizeEmail(authenticatedEmail)));
    }

    /**
     * Registra un nuevo usuario final.
     *
     * @param registrationCommand datos de alta normalizados desde la capa web
     * @return sesión inicial del usuario creado
     */
    @Override
    public AuthResponse register(AuthRegistrationCommand registrationCommand) {
        String normalizedEmail = normalizeEmail(registrationCommand.email());
        User createdUser = userUseCase.createUser(
                buildUser(registrationCommand, normalizedEmail),
                registrationCommand.rawPassword()
        );
        return buildAuthResponse(createdUser);
    }

    /**
     * Registra un nuevo organizador con sus datos específicos.
     *
     * <p>El usuario asociado al organizador se crea con los mismos criterios que un registro normal, pero se
     * completa con la razón social y el CIF necesarios para la entidad organizadora.</p>
     *
     * @param organizerRegistrationCommand datos de alta del organizador y de su usuario asociado
     * @return sesión inicial del usuario del organizador
     */
    @Override
    public AuthResponse registerOrganizer(OrganizerRegistrationCommand organizerRegistrationCommand) {
        AuthRegistrationCommand registrationCommand = organizerRegistrationCommand.authRegistration();
        String normalizedEmail = normalizeEmail(registrationCommand.email());

        Organizer organizer = new Organizer();
        organizer.setUser(buildUser(registrationCommand, normalizedEmail));
        organizer.setLegalName(normalizeText(organizerRegistrationCommand.legalName()));
        organizer.setCif(normalizeText(organizerRegistrationCommand.cif()));

        Organizer createdOrganizer = organizerUseCase.createOrganizer(organizer, registrationCommand.rawPassword());
        return buildAuthResponse(createdOrganizer.getUser());
    }

    // Carga solo usuarios activos para que login y sesión compartan la misma regla.
    private User loadActiveUserByEmail(String normalizedEmail) {
        return userPersistencePort.findByEmail(normalizedEmail)
                .filter(existingUser -> Boolean.TRUE.equals(existingUser.getEnabled()))
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));
    }

    // Construye el usuario base antes de delegar sus validaciones al caso de uso correspondiente.
    private User buildUser(AuthRegistrationCommand registrationCommand, String normalizedEmail) {
        User user = new User();
        user.setDisplayName(normalizeText(registrationCommand.displayName()));
        user.setEmail(normalizedEmail);
        user.setName(normalizeText(registrationCommand.name()));
        user.setSurname(normalizeText(registrationCommand.surname()));
        user.setAddress(normalizeText(registrationCommand.address()));
        user.setPhone(normalizeText(registrationCommand.phone()));
        return user;
    }

    // Reduce la entidad User al contrato de sesión que consume el frontend.
    private AuthResponse buildAuthResponse(User user) {
        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setRoleName(user.getRole() != null ? user.getRole().getRoleName() : null);
        return response;
    }

    // Normaliza el correo para búsquedas y comparaciones.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Normaliza textos obligatorios recibidos desde el formulario de registro.
    private String normalizeText(String value) {
        return value.trim();
    }
}
