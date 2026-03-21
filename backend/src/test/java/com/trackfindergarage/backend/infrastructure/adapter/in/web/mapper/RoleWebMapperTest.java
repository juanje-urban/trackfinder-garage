package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateRoleRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.RoleResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateRoleRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleWebMapperTest {

    private final RoleWebMapper roleWebMapper = new RoleWebMapper();

    @Test
    void toDomainMapsCreateRequestToRole() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRole("ADMIN");

        Role role = roleWebMapper.toDomain(request);

        assertEquals("ADMIN", role.getRoleName());
    }

    @Test
    void updateDomainMapsUpdateRequestToExistingRole() {
        Role role = new Role();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("USER");

        roleWebMapper.updateDomain(role, request);

        assertEquals("USER", role.getRoleName());
    }

    @Test
    void toResponseMapsRoleToResponse() {
        Role role = new Role();
        role.setId(5L);
        role.setRoleName("ORGANIZER");

        RoleResponse response = roleWebMapper.toResponse(role);

        assertEquals(5L, response.getId());
        assertEquals("ORGANIZER", response.getRole());
    }
}
