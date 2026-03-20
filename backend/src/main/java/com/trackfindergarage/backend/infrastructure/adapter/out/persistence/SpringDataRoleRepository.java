package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String role);
}