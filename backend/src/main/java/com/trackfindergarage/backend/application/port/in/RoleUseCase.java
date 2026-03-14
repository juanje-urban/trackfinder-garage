package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Role;

import java.util.List;

public interface RoleUseCase {

    Role createRole(Role role);

    Role updateRole(Long id, Role role);

    void deleteRole(Long id);

    List<Role> getAllRoles();

    Role getRoleById(Long id);
}