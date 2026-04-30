package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link User}.
 *
 * <p>Declara búsquedas habituales de usuarios y carga el rol cuando la aplicación necesita trabajar
 * con la identidad completa del usuario.</p>
 */
public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    @Override
    @EntityGraph(attributePaths = "role")
    java.util.List<User> findAll();

    @Override
    @EntityGraph(attributePaths = "role")
    Optional<User> findById(Long id);

    Optional<User> findByDisplayName(String displayName);

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
}
