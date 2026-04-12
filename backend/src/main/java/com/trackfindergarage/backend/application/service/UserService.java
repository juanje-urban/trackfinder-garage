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

@Service
@Transactional
public class UserService implements UserUseCase {

    private static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    private static final String USER_DISPLAY_NAME_ALREADY_EXISTS = "User with display name '%s' already exists";
    private static final String USER_EMAIL_ALREADY_EXISTS = "User with email '%s' already exists";
    private static final String USER_PHONE_ALREADY_EXISTS = "User with phone '%s' already exists";
    private static final String ORGANIZER_USERS_MANAGED_THROUGH_ORGANIZER_SERVICE =
            "Organizer users must be managed through OrganizerService";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "Authenticated user email is required";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@example.com";
    private static final String DEFAULT_ADMIN_ACCOUNT_CANNOT_BE_DISABLED =
            "The default administrator account cannot be disabled";

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

    @Override
    public User createUser(User user, String rawPassword) {
        validateDisplayNameForCreate(user.getDisplayName());
        validateEmailForCreate(user.getEmail());
        validatePhoneForCreate(user.getPhone());

        Role role = getUserRole();

        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);

        return userPersistencePort.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String authenticatedEmail) {
        return findUserByAuthenticatedEmail(authenticatedEmail);
    }

    @Override
    public User updateCurrentUserProfile(String authenticatedEmail,
                                         String name,
                                         String surname,
                                         String email,
                                         String address,
                                         String phone,
                                         String rawPassword) {
        User existingUser = findUserByAuthenticatedEmail(authenticatedEmail);

        if (isOrganizerUser(existingUser)) {
            throw new IllegalArgumentException(ORGANIZER_USERS_MANAGED_THROUGH_ORGANIZER_SERVICE);
        }

        String normalizedName = normalizeText(name);
        String normalizedSurname = normalizeText(surname);
        String normalizedEmail = normalizeEmail(email);
        String normalizedAddress = normalizeText(address);
        String normalizedPhone = normalizeText(phone);

        validateEmailForUpdate(existingUser.getId(), normalizedEmail);
        validatePhoneForUpdate(existingUser.getId(), normalizedPhone);

        existingUser.setName(normalizedName);
        existingUser.setSurname(normalizedSurname);
        existingUser.setEmail(normalizedEmail);
        existingUser.setAddress(normalizedAddress);
        existingUser.setPhone(normalizedPhone);

        if (rawPassword != null && !rawPassword.isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
        }

        return userPersistencePort.save(existingUser);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = findUserOrThrow(id);

        if (isOrganizerUser(existingUser)) {
            throw new IllegalArgumentException(ORGANIZER_USERS_MANAGED_THROUGH_ORGANIZER_SERVICE);
        }

        validateDisplayNameForUpdate(id, user.getDisplayName());
        validateEmailForUpdate(id, user.getEmail());
        validatePhoneForUpdate(id, user.getPhone());

        existingUser.setDisplayName(user.getDisplayName());
        existingUser.setEmail(user.getEmail());
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhone(user.getPhone());

        return userPersistencePort.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User existingUser = findUserOrThrow(id);

        organizerPersistencePort.findById(id)
                .ifPresent(organizerPersistencePort::delete);

        userPersistencePort.delete(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userPersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public User enableUser(Long id) {
        User existingUser = findUserOrThrow(id);
        existingUser.setEnabled(true);
        return userPersistencePort.save(existingUser);
    }

    @Override
    public User disableUser(Long id) {
        User existingUser = findUserOrThrow(id);

        if (isDefaultAdminUser(existingUser)) {
            throw new IllegalArgumentException(DEFAULT_ADMIN_ACCOUNT_CANNOT_BE_DISABLED);
        }

        existingUser.setEnabled(false);

        organizerPersistencePort.findById(id)
                .ifPresent(organizer -> {
                    organizer.setEnabled(false);
                    organizerPersistencePort.save(organizer);
                });

        return userPersistencePort.save(existingUser);
    }

    private Role getUserRole() {
        return rolePersistencePort.findByRoleName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: USER"));
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
        userPersistencePort.findByDisplayName(displayName)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(USER_DISPLAY_NAME_ALREADY_EXISTS.formatted(displayName));
                });
    }

    private void validateEmailForCreate(String email) {
        userPersistencePort.findByEmail(email)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(USER_EMAIL_ALREADY_EXISTS.formatted(email));
                });
    }

    private void validateDisplayNameForUpdate(Long userId, String displayName) {
        userPersistencePort.findByDisplayName(displayName)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new DuplicateResourceException(USER_DISPLAY_NAME_ALREADY_EXISTS.formatted(displayName));
                    }
                });
    }

    private void validatePhoneForCreate(String phone) {
        userPersistencePort.findByPhone(phone)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(USER_PHONE_ALREADY_EXISTS.formatted(phone));
                });
    }

    private void validateEmailForUpdate(Long userId, String email) {
        userPersistencePort.findByEmail(email)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new DuplicateResourceException(USER_EMAIL_ALREADY_EXISTS.formatted(email));
                    }
                });
    }

    private void validatePhoneForUpdate(Long userId, String phone) {
        userPersistencePort.findByPhone(phone)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new DuplicateResourceException(USER_PHONE_ALREADY_EXISTS.formatted(phone));
                    }
                });
    }

    //Función privada que hace lo mismo que getUserById. Los métodos con proxy de Spring no deben ser llamados desde dentro del propio bean. (Da error sonar)
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

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value.trim();
    }
}
