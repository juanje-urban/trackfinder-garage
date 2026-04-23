package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.RolePersistencePort;
import com.trackfindergarage.backend.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RolePersistenceAdapter implements RolePersistencePort {

    private final SpringDataRoleRepository springDataRoleRepository;

    public RolePersistenceAdapter(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Optional<Role> findByRoleName(String role) {
        return springDataRoleRepository.findByRoleName(role);
    }
}
