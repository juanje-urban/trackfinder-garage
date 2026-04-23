package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Role;

import java.util.Optional;

public interface RolePersistencePort {

    Optional<Role> findByRoleName(String role);
}
