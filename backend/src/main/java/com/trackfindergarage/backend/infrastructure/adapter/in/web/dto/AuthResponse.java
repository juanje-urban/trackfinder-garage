package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO de salida con la información minima de sesión del usuario autenticado.
 */
@Getter
@Setter
public class AuthResponse {

    /**
     * Identificador interno del usuario autenticado.
     */
    private Long userId;

    /**
     * Alias público mostrado en la aplicación.
     */
    private String displayName;

    /**
     * Email de acceso del usuario.
     */
    private String email;

    /**
     * Nombre del rol asignado al usuario autenticado.
     */
    private String roleName;
}
