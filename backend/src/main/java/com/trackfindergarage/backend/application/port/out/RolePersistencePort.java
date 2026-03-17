package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RolePersistencePort {

    Role save(Role role);

    Optional<Role> findById(Long id);

    Optional<Role> findByRole(String role);

    List<Role> findAll();

    void delete(Role role);
}