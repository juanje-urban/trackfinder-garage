package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.RolePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private RolePersistencePort rolePersistencePort;

    @Mock
    private OrganizerPersistencePort organizerPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserAssignsDefaultRoleEncodesPasswordAndEnablesUser() {
        User user = userWithId(null, "driver");
        Role userRole = roleWithId(1L, "USER");

        when(userPersistencePort.findByDisplayName("driver")).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.empty());
        when(rolePersistencePort.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plain-pass")).thenReturn("hashed-pass");
        when(userPersistencePort.save(user)).then(returnsFirstArg());

        User createdUser = userService.createUser(user, "plain-pass");

        assertSame(user, createdUser);
        assertSame(userRole, createdUser.getRole());
        assertEquals("hashed-pass", createdUser.getPasswordHash());
        assertTrue(createdUser.getEnabled());
        assertNotNull(createdUser.getCreated());
        verify(userPersistencePort).save(user);
    }

    @Test
    void createUserThrowsWhenDisplayNameAlreadyExists() {
        User user = userWithId(null, "driver");

        when(userPersistencePort.findByDisplayName("driver")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(user, "plain-pass"));
        verify(userPersistencePort, never()).save(user);
    }

    @Test
    void createUserThrowsWhenDefaultRoleDoesNotExist() {
        User user = userWithId(null, "driver");

        when(userPersistencePort.findByDisplayName("driver")).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.empty());
        when(rolePersistencePort.findByRoleName("USER")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.createUser(user, "plain-pass"));
        verify(userPersistencePort, never()).save(user);
    }

    @Test
    void updateUserCopiesEditableFieldsAndSaves() {
        User existingUser = userWithId(10L, "driver");
        existingUser.setRole(roleWithId(1L, "USER"));

        User updateRequest = userWithId(null, "new-driver");
        updateRequest.setName("New");
        updateRequest.setSurname("Name");
        updateRequest.setAddress("New address");
        updateRequest.setPhone("999");
        updateRequest.setEmail("new@example.com");

        when(userPersistencePort.findById(10L)).thenReturn(Optional.of(existingUser));
        when(userPersistencePort.findByDisplayName("new-driver")).thenReturn(Optional.empty());
        when(userPersistencePort.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userPersistencePort.save(existingUser)).thenReturn(existingUser);

        User updatedUser = userService.updateUser(10L, updateRequest);

        assertSame(existingUser, updatedUser);
        assertEquals("new-driver", existingUser.getDisplayName());
        assertEquals("new@example.com", existingUser.getEmail());
        assertEquals("New", existingUser.getName());
        assertEquals("Name", existingUser.getSurname());
        assertEquals("New address", existingUser.getAddress());
        assertEquals("999", existingUser.getPhone());
        verify(userPersistencePort).save(existingUser);
    }

    @Test
    void updateUserThrowsForOrganizerUsers() {
        User existingUser = userWithId(10L, "organizer-user");
        existingUser.setRole(roleWithId(2L, "ORGANIZER"));

        when(userPersistencePort.findById(10L)).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(10L, new User()));
    }

    @Test
    void deleteUserDeletesOrganizerProjectionWhenPresent() {
        User existingUser = userWithId(4L, "driver");
        Organizer organizer = new Organizer();
        organizer.setIdUser(4L);
        organizer.setUser(existingUser);

        when(userPersistencePort.findById(4L)).thenReturn(Optional.of(existingUser));
        when(organizerPersistencePort.findById(4L)).thenReturn(Optional.of(organizer));

        userService.deleteUser(4L);

        verify(organizerPersistencePort).delete(organizer);
        verify(userPersistencePort).delete(existingUser);
    }

    @Test
    void getAllUsersReturnsPersistenceResult() {
        List<User> users = List.of(userWithId(1L, "one"), userWithId(2L, "two"));

        when(userPersistencePort.findAll()).thenReturn(users);

        assertEquals(users, userService.getAllUsers());
    }

    @Test
    void enableUserMarksUserAsEnabled() {
        User existingUser = userWithId(5L, "driver");
        existingUser.setEnabled(false);

        when(userPersistencePort.findById(5L)).thenReturn(Optional.of(existingUser));
        when(userPersistencePort.save(existingUser)).thenReturn(existingUser);

        User enabledUser = userService.enableUser(5L);

        assertTrue(enabledUser.getEnabled());
        verify(userPersistencePort).save(existingUser);
    }

    @Test
    void disableUserDisablesOrganizerTooWhenPresent() {
        User existingUser = userWithId(5L, "driver");
        existingUser.setEnabled(true);

        Organizer organizer = new Organizer();
        organizer.setIdUser(5L);
        organizer.setUser(existingUser);
        organizer.setEnabled(true);

        when(userPersistencePort.findById(5L)).thenReturn(Optional.of(existingUser));
        when(organizerPersistencePort.findById(5L)).thenReturn(Optional.of(organizer));
        when(userPersistencePort.save(existingUser)).thenReturn(existingUser);

        User disabledUser = userService.disableUser(5L);

        assertEquals(Boolean.FALSE, disabledUser.getEnabled());
        assertEquals(Boolean.FALSE, organizer.getEnabled());
        verify(organizerPersistencePort).save(organizer);
        verify(userPersistencePort).save(existingUser);
    }

    @Test
    void getUserByIdThrowsWhenUserDoesNotExist() {
        when(userPersistencePort.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(42L));
    }

    private User userWithId(Long id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName + "@example.com");
        user.setName("Name");
        user.setSurname("Surname");
        user.setAddress("Address");
        user.setPhone("123456789");
        return user;
    }

    private Role roleWithId(Long id, String value) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(value);
        return role;
    }
}
