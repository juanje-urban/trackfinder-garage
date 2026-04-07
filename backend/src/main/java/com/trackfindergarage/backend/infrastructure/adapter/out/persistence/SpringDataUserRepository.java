package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByDisplayName(String displayName);

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
}
