package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.RolePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RolePersistencePort rolePersistencePort;

    @InjectMocks
    private RoleService roleService;

    @Test
    void createRoleSavesWhenRoleDoesNotExist() {
        Role role = roleWithId(1L, "USER");

        when(rolePersistencePort.findByRoleName("USER")).thenReturn(Optional.empty());
        when(rolePersistencePort.save(role)).thenReturn(role);

        Role createdRole = roleService.createRole(role);

        assertSame(role, createdRole);
        verify(rolePersistencePort).save(role);
    }

    @Test
    void createRoleThrowsWhenRoleAlreadyExists() {
        Role role = roleWithId(1L, "USER");

        when(rolePersistencePort.findByRoleName("USER")).thenReturn(Optional.of(role));

        assertThrows(DuplicateResourceException.class, () -> roleService.createRole(role));
        verify(rolePersistencePort, never()).save(role);
    }

    @Test
    void updateRoleChangesValueAndSaves() {
        Role existingRole = roleWithId(5L, "USER");
        Role updateRequest = roleWithId(null, "ADMIN");

        when(rolePersistencePort.findById(5L)).thenReturn(Optional.of(existingRole));
        when(rolePersistencePort.findByRoleName("ADMIN")).thenReturn(Optional.empty());
        when(rolePersistencePort.save(existingRole)).thenReturn(existingRole);

        Role updatedRole = roleService.updateRole(5L, updateRequest);

        assertSame(existingRole, updatedRole);
        assertEquals("ADMIN", existingRole.getRoleName());
        verify(rolePersistencePort).save(existingRole);
    }

    @Test
    void updateRoleThrowsWhenAnotherRoleUsesSameValue() {
        Role existingRole = roleWithId(5L, "USER");
        Role otherRole = roleWithId(9L, "ADMIN");
        Role updateRequest = roleWithId(null, "ADMIN");

        when(rolePersistencePort.findById(5L)).thenReturn(Optional.of(existingRole));
        when(rolePersistencePort.findByRoleName("ADMIN")).thenReturn(Optional.of(otherRole));

        assertThrows(DuplicateResourceException.class, () -> roleService.updateRole(5L, updateRequest));
        verify(rolePersistencePort, never()).save(existingRole);
    }

    @Test
    void deleteRoleRemovesExistingRole() {
        Role existingRole = roleWithId(3L, "USER");

        when(rolePersistencePort.findById(3L)).thenReturn(Optional.of(existingRole));

        roleService.deleteRole(3L);

        verify(rolePersistencePort).delete(existingRole);
    }

    @Test
    void getAllRolesReturnsPersistenceResult() {
        List<Role> roles = List.of(roleWithId(1L, "USER"), roleWithId(2L, "ORGANIZER"));

        when(rolePersistencePort.findAll()).thenReturn(roles);

        assertEquals(roles, roleService.getAllRoles());
    }

    @Test
    void getRoleByIdThrowsWhenRoleDoesNotExist() {
        when(rolePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleById(99L));
    }

    private Role roleWithId(Long id, String value) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(value);
        return role;
    }
}
