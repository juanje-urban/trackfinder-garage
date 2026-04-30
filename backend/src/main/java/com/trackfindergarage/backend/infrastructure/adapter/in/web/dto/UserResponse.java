package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO de salida con la vista completa de un usuario.
 *
 * <p>Expone los datos de identidad, contacto, activación y rol que el frontend necesita para
 * representar el perfil detallado del usuario.</p>
 */
@Getter
@Builder
public class UserResponse {

    private Long id;
    private String displayName;
    private String email;
    private LocalDateTime created;
    private Boolean enabled;
    private String name;
    private String surname;
    private String address;
    private String phone;
    private Long roleId;
    private String roleName;
}
