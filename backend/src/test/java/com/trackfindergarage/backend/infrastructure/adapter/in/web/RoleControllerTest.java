package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.RoleUseCase;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateRoleRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.RoleResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateRoleRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.RoleWebMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleControllerTest {

    private final RoleUseCase roleUseCase = mock(RoleUseCase.class);
    private final RoleWebMapper roleWebMapper = new RoleWebMapper();
    private final RoleController roleController = new RoleController(roleUseCase, roleWebMapper);

    @Test
    void createRoleDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRole("ADMIN");

        when(roleUseCase.createRole(any(Role.class))).thenReturn(roleWithId(1L, "ADMIN"));

        RoleResponse response = roleController.createRole(request);

        assertEquals(1L, response.getId());
        assertEquals("ADMIN", response.getRole());
        verify(roleUseCase).createRole(any(Role.class));
    }

    @Test
    void updateRoleDelegatesToUseCaseAndReturnsMappedResponse() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("USER");

        when(roleUseCase.updateRole(eq(2L), any(Role.class))).thenReturn(roleWithId(2L, "USER"));

        RoleResponse response = roleController.updateRole(2L, request);

        assertEquals(2L, response.getId());
        assertEquals("USER", response.getRole());
        verify(roleUseCase).updateRole(eq(2L), any(Role.class));
    }

    @Test
    void getAllRolesMapsUseCaseResult() {
        when(roleUseCase.getAllRoles()).thenReturn(List.of(roleWithId(1L, "USER")));

        List<RoleResponse> response = roleController.getAllRoles();

        assertEquals(1, response.size());
        assertEquals("USER", response.getFirst().getRole());
    }

    @Test
    void getRoleByIdReturnsMappedResponse() {
        when(roleUseCase.getRoleById(3L)).thenReturn(roleWithId(3L, "ORGANIZER"));

        RoleResponse response = roleController.getRoleById(3L);

        assertEquals(3L, response.getId());
        assertEquals("ORGANIZER", response.getRole());
    }

    @Test
    void deleteRoleDelegatesToUseCase() {
        roleController.deleteRole(4L);

        verify(roleUseCase).deleteRole(4L);
    }

    private Role roleWithId(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(name);
        return role;
    }
}
