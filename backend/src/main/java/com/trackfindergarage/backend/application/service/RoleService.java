package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.RoleUseCase;
import com.trackfindergarage.backend.application.port.out.RolePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleService implements RoleUseCase {

    private final RolePersistencePort rolePersistencePort;

    public RoleService(RolePersistencePort rolePersistencePort) {
        this.rolePersistencePort = rolePersistencePort;
    }

    @Override
    public Role createRole(Role role) {
        rolePersistencePort.findByRoleName(role.getRoleName())
                .ifPresent(existingRole -> {
                    throw new DuplicateResourceException(
                            "Role with value '" + role.getRoleName() + "' already exists"
                    );
                });

        return rolePersistencePort.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {
        Role existingRole = getRoleById(id);

        rolePersistencePort.findByRoleName(role.getRoleName())
                .ifPresent(foundRole -> {
                    if (!foundRole.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                "Role with value '" + role.getRoleName() + "' already exists"
                        );
                    }
                });

        existingRole.setRoleName(role.getRoleName());

        return rolePersistencePort.save(existingRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role existingRole = getRoleById(id);
        rolePersistencePort.delete(existingRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return rolePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return rolePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }
}