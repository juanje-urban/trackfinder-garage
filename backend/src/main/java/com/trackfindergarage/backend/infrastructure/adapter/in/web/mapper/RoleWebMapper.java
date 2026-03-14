package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateRoleRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.RoleResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateRoleRequest;
import org.springframework.stereotype.Component;

@Component
public class RoleWebMapper {

    public Role toDomain(CreateRoleRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        return role;
    }

    public void updateDomain(Role role, UpdateRoleRequest request) {
        role.setName(request.getName());
    }

    public RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}