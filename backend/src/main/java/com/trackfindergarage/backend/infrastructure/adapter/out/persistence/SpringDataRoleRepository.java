package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoleRepository extends JpaRepository<Role, Long> {
}