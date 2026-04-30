package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Role;

import java.util.Optional;

/**
 * Puerto de salida para consultar roles de usuario.
 *
 * <p>Se usa como punto de acceso mínimo al catálogo de roles persistidos desde la capa de
 * aplicación.</p>
 */
public interface RolePersistencePort {

    Optional<Role> findByRoleName(String role);
}
