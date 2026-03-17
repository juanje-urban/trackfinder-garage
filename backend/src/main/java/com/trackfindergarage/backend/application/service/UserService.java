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

@Service
@Transactional
public class UserService implements UserUseCase {

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

        Role role = getUserRole();

        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);

        return userPersistencePort.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);

        if (isOrganizerUser(existingUser)) {
            throw new IllegalArgumentException(
                    "Organizer users must be managed through OrganizerService"
            );
        }

        validateDisplayNameForUpdate(id, user.getDisplayName());
        validateEmailForUpdate(id, user.getEmail());

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
        User existingUser = getUserById(id);

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public User enableUser(Long id) {
        User existingUser = getUserById(id);
        existingUser.setEnabled(true);
        return userPersistencePort.save(existingUser);
    }

    @Override
    public User disableUser(Long id) {
        User existingUser = getUserById(id);
        existingUser.setEnabled(false);

        organizerPersistencePort.findById(id)
                .ifPresent(organizer -> {
                    organizer.setEnabled(false);
                    organizerPersistencePort.save(organizer);
                });

        return userPersistencePort.save(existingUser);
    }

    private Role getUserRole() {
        return rolePersistencePort.findByRole("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: USER"));
    }

    private boolean isOrganizerUser(User user) {
        return user.getRole() != null
                && user.getRole().getRole() != null
                && "ORGANIZER".equalsIgnoreCase(user.getRole().getRole().trim());
    }

    private void validateDisplayNameForCreate(String displayName) {
        userPersistencePort.findByDisplayName(displayName)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(
                            "User with display name '" + displayName + "' already exists"
                    );
                });
    }

    private void validateEmailForCreate(String email) {
        userPersistencePort.findByEmail(email)
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(
                            "User with email '" + email + "' already exists"
                    );
                });
    }

    private void validateDisplayNameForUpdate(Long userId, String displayName) {
        userPersistencePort.findByDisplayName(displayName)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new DuplicateResourceException(
                                "User with display name '" + displayName + "' already exists"
                        );
                    }
                });
    }

    private void validateEmailForUpdate(Long userId, String email) {
        userPersistencePort.findByEmail(email)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new DuplicateResourceException(
                                "User with email '" + email + "' already exists"
                        );
                    }
                });
    }
}