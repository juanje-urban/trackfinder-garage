package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.RolePersistencePort;
import com.trackfindergarage.backend.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RolePersistenceAdapter implements RolePersistencePort {

    private final SpringDataRoleRepository springDataRoleRepository;

    public RolePersistenceAdapter(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Role save(Role role) {
        return springDataRoleRepository.save(role);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return springDataRoleRepository.findById(id);
    }

    @Override
    public Optional<Role> findByRoleName(String role) {
        return springDataRoleRepository.findByRoleName(role);
    }

    @Override
    public List<Role> findAll() {
        return springDataRoleRepository.findAll();
    }

    @Override
    public void delete(Role role) {
        springDataRoleRepository.delete(role);
    }
}