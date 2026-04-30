package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link Role}.
 *
 * <p>Expone las búsquedas mínimas del catálogo de roles utilizadas por la infraestructura de
 * autenticación y registro.</p>
 */
public interface SpringDataRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String role);
}
