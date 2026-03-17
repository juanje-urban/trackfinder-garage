package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.UserUseCase;
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
import java.util.Set;

@Service
@Transactional
public class UserService implements UserUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ORGANIZER");

    private final UserPersistencePort userPersistencePort;
    private final RolePersistencePort rolePersistencePort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserPersistencePort userPersistencePort,
                       RolePersistencePort rolePersistencePort,
                       PasswordEncoder passwordEncoder) {
        this.userPersistencePort = userPersistencePort;
        this.rolePersistencePort = rolePersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(User user, String rawPassword, Long roleId) {
        validateDisplayNameForCreate(user.getDisplayName());
        validateEmailForCreate(user.getEmail());

        Role role = getRoleById(roleId);

        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);

        return userPersistencePort.save(user);
    }

    @Override
    public User updateUser(Long id, User user, Long roleId) {
        User existingUser = getUserById(id);

        validateDisplayNameForUpdate(id, user.getDisplayName());
        validateEmailForUpdate(id, user.getEmail());

        Role role = getRoleById(roleId);

        existingUser.setDisplayName(user.getDisplayName());
        existingUser.setEmail(user.getEmail());
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());
        existingUser.setAddress(user.getAddress());
        existingUser.setPhone(user.getPhone());
        existingUser.setRole(role);

        return userPersistencePort.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User existingUser = getUserById(id);
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
        return userPersistencePort.save(existingUser);
    }

    private Role getRoleById(Long roleId) {
        Role role = rolePersistencePort.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        String roleName = role.getRole() == null ? "" : role.getRole().trim().toUpperCase();

        if (!ALLOWED_ROLES.contains(roleName)) {
            throw new IllegalArgumentException(
                    "Only USER and ORGANIZER roles are allowed in UserService"
            );
        }

        return role;
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