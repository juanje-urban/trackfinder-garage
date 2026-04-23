package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.RolePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementa la lógica de gestion de usuarios no organizadores.
 *
 * <p>Se encarga de altas, consultas, actualizaciones, habilitación y deshabilitación de cuentas, asi como de las
 * validaciones de unicidad sobre alias, email y teléfono.</p>
 */
@Service
@Transactional
public class UserService implements UserUseCase {

    private static final String USER_NOT_FOUND_WITH_ID = "Usuario no encontrado con id: ";
    private static final String USER_DISPLAY_NAME_ALREADY_EXISTS = "Ya existe un usuario con el nombre visible '%s'";
    private static final String USER_EMAIL_ALREADY_EXISTS = "Ya existe un usuario con el correo electrónico '%s'";
    private static final String USER_PHONE_ALREADY_EXISTS = "Ya existe un usuario con el teléfono '%s'";
    private static final String ORGANIZER_USERS_MANAGED_THROUGH_ORGANIZER_SERVICE =
            "Los usuarios organizadores deben gestionarse a través de OrganizerService";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "El correo electrónico del usuario autenticado es obligatorio";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "Usuario no encontrado con correo electrónico: ";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@example.com";
    private static final String DEFAULT_ADMIN_ACCOUNT_CANNOT_BE_DISABLED =
            "La cuenta de administrador por defecto no puede deshabilitarse";

    private final UserPersistencePort userPersistencePort;
    private final RolePersistencePort rolePersistencePort;
    private final OrganizerPersistencePort organizerPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserPersistencePort userPersistencePort,
                       RolePersistencePort rolePersistencePort,
                       OrganizerPersistencePort organizerPersistencePort,
                       PasswordEncoder passwordEncoder) {
        this.userPersistencePort = userPersistencePort;
        this.rolePersistencePort = rolePersistencePort;
        this.organizerPersistencePort = organizerPersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea un nuevo usuario final con su rol, contraseña codificada y fecha de alta.
     *
     * @param user datos del usuario
     * @param rawPassword contraseña en texto plano
     * @return usuario persistido
     */
    @Override
    public User createUser(User user, String rawPassword) {
        String normalizedDisplayName = normalizeText(user.getDisplayName());
        String normalizedEmail = normalizeEmail(user.getEmail());
        String normalizedName = normalizeText(user.getName());
        String normalizedSurname = normalizeText(user.getSurname());
        String normalizedAddress = normalizeText(user.getAddress());
        String normalizedPhone = normalizeText(user.getPhone());

        validateDisplayNameForCreate(normalizedDisplayName);
        validateEmailForCreate(normalizedEmail);
        validatePhoneForCreate(normalizedPhone);

        applyUserIdentity(
                user,
                normalizedDisplayName,
                normalizedEmail,
                normalizedName,
                normalizedSurname,
                normalizedAddress,
                normalizedPhone
        );
        user.setRole(getUserRole());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);

        return userPersistencePort.save(user);
    }

    /**
     * Obtiene el usuario autenticado en la petición actual.
     *
     * @param authenticatedEmail email resuelto por la capa de seguridad
     * @return usuario autenticado
     */
    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String authenticatedEmail) {
        return findUserByAuthenticatedEmail(authenticatedEmail);
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * <p>Este flujo está reservado a usuarios finales. Si la cuenta pertenece a un organizador, la actualización
     * debe pasar por {@code OrganizerService}.</p>
     *
     * @param authenticatedEmail email del usuario autenticado
     * @param name nombre actualizado
     * @param surname apellidos actualizados
     * @param email email actualizado
     * @param address dirección actualizada
     * @param phone teléfono actualizado
     * @param rawPassword nueva contraseña opcional
     * @return usuario actualizado
     */
    @Override
    public User updateCurrentUserProfile(String authenticatedEmail,
                                         String name,
                                         String surname,
                                         String email,
                                         String address,
                                         String phone,
                                         String rawPassword) {
        User existingUser = findUserByAuthenticatedEmail(authenticatedEmail);
        requireNonOrganizerUser(existingUser);

        String normalizedName = normalizeText(name);
        String normalizedSurname = normalizeText(surname);
        String normalizedEmail = normalizeEmail(email);
        String normalizedAddress = normalizeText(address);
        String normalizedPhone = normalizeText(phone);

        validateEmailForUpdate(existingUser.getId(), normalizedEmail);
        validatePhoneForUpdate(existingUser.getId(), normalizedPhone);
        applyUserProfile(existingUser, normalizedName, normalizedSurname, normalizedEmail, normalizedAddress, normalizedPhone);

        if (rawPassword != null && !rawPassword.isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
        }

        return userPersistencePort.save(existingUser);
    }

    /**
     * Recupera todos los usuarios.
     *
     * @return listado completo de usuarios
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userPersistencePort.findAll();
    }

    /**
     * Habilita una cuenta de usuario.
     *
     * @param id identificador del usuario
     * @return usuario habilitado
     */
    @Override
    public User enableUser(Long id) {
        User existingUser = findUserOrThrow(id);
        existingUser.setEnabled(true);
        return userPersistencePort.save(existingUser);
    }

    /**
     * Deshabilita una cuenta de usuario.
     *
     * <p>La cuenta de administrador por defecto queda protegida y, si el usuario tiene información de organizador
     * asociada, esta también se deshabilita.</p>
     *
     * @param id identificador del usuario
     * @return usuario deshabilitado
     */
    @Override
    public User disableUser(Long id) {
        User existingUser = findUserOrThrow(id);

        if (isDefaultAdminUser(existingUser)) {
            throw new IllegalArgumentException(DEFAULT_ADMIN_ACCOUNT_CANNOT_BE_DISABLED);
        }

        existingUser.setEnabled(false);
        disableOrganizerIfPresent(id);
        return userPersistencePort.save(existingUser);
    }

    private Role getUserRole() {
        return rolePersistencePort.findByRoleName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: USER"));
    }

    private void requireNonOrganizerUser(User user) {
        if (isOrganizerUser(user)) {
            throw new IllegalArgumentException(ORGANIZER_USERS_MANAGED_THROUGH_ORGANIZER_SERVICE);
        }
    }

    private boolean isOrganizerUser(User user) {
        return user.getRole() != null
                && user.getRole().getRoleName() != null
                && "ORGANIZER".equalsIgnoreCase(user.getRole().getRoleName().trim());
    }

    private boolean isDefaultAdminUser(User user) {
        if (user.getRole() == null || user.getRole().getRoleName() == null || user.getEmail() == null) {
            return false;
        }

        return "ADMIN".equalsIgnoreCase(user.getRole().getRoleName().trim())
                && DEFAULT_ADMIN_EMAIL.equals(normalizeEmail(user.getEmail()));
    }

    private void validateDisplayNameForCreate(String displayName) {
        validateUniqueUser(
                userPersistencePort.findByDisplayName(displayName),
                null,
                USER_DISPLAY_NAME_ALREADY_EXISTS.formatted(displayName)
        );
    }

    private void validateEmailForCreate(String email) {
        validateUniqueUser(
                userPersistencePort.findByEmail(email),
                null,
                USER_EMAIL_ALREADY_EXISTS.formatted(email)
        );
    }

    private void validatePhoneForCreate(String phone) {
        validateUniqueUser(
                userPersistencePort.findByPhone(phone),
                null,
                USER_PHONE_ALREADY_EXISTS.formatted(phone)
        );
    }

    private void validateEmailForUpdate(Long userId, String email) {
        validateUniqueUser(
                userPersistencePort.findByEmail(email),
                userId,
                USER_EMAIL_ALREADY_EXISTS.formatted(email)
        );
    }

    private void validatePhoneForUpdate(Long userId, String phone) {
        validateUniqueUser(
                userPersistencePort.findByPhone(phone),
                userId,
                USER_PHONE_ALREADY_EXISTS.formatted(phone)
        );
    }

    private void validateUniqueUser(Optional<User> candidate,
                                    Long excludedUserId,
                                    String duplicateMessage) {
        candidate.ifPresent(existingUser -> {
            if (excludedUserId == null || !existingUser.getId().equals(excludedUserId)) {
                throw new DuplicateResourceException(duplicateMessage);
            }
        });
    }

    private User findUserOrThrow(Long id) {
        return userPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));
    }

    private User findUserByAuthenticatedEmail(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = normalizeEmail(authenticatedEmail);
        return userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail));
    }

    private void applyUserIdentity(User user,
                                   String displayName,
                                   String email,
                                   String name,
                                   String surname,
                                   String address,
                                   String phone) {
        user.setDisplayName(displayName);
        applyUserProfile(user, name, surname, email, address, phone);
    }

    private void applyUserProfile(User user,
                                  String name,
                                  String surname,
                                  String email,
                                  String address,
                                  String phone) {
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhone(phone);
    }

    private void disableOrganizerIfPresent(Long userId) {
        organizerPersistencePort.findById(userId)
                .ifPresent(organizer -> {
                    organizer.setEnabled(false);
                    organizerPersistencePort.save(organizer);
                });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value.trim();
    }
}
