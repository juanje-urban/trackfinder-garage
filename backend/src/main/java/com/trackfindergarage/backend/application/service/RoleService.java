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

    private static final String ROLE_NOT_FOUND_WITH_ID = "Role not found with id: ";
    private static final String ROLE_ALREADY_EXISTS = "Role with value '%s' already exists";

    private final RolePersistencePort rolePersistencePort;

    public RoleService(RolePersistencePort rolePersistencePort) {
        this.rolePersistencePort = rolePersistencePort;
    }

    @Override
    public Role createRole(Role role) {
        rolePersistencePort.findByRoleName(role.getRoleName())
                .ifPresent(existingRole -> {
                    throw new DuplicateResourceException(ROLE_ALREADY_EXISTS.formatted(role.getRoleName()));
                });

        return rolePersistencePort.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {
        Role existingRole = findRoleOrThrow(id);

        rolePersistencePort.findByRoleName(role.getRoleName())
                .ifPresent(foundRole -> {
                    if (!foundRole.getId().equals(id)) {
                        throw new DuplicateResourceException(ROLE_ALREADY_EXISTS.formatted(role.getRoleName()));
                    }
                });

        existingRole.setRoleName(role.getRoleName());

        return rolePersistencePort.save(existingRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role existingRole = findRoleOrThrow(id);
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
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_ID + id));
    }

    //Función privada que hace lo mismo que getRoleById. Los métodos con proxy de Spring no deben ser llamados desde dentro del propio bean. (Da error sonar)
    private Role findRoleOrThrow(Long id) {
        return rolePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROLE_NOT_FOUND_WITH_ID + id));
    }
}
