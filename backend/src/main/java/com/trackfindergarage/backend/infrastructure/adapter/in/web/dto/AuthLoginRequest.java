package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para el inicio de sesión.
 */
@Getter
@Setter
public class AuthLoginRequest {

    /**
     * Email del usuario quiere autenticarse.
     */
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    /**
     * Contraseña en texto plano.
     */
    @NotBlank
    @Size(max = 255)
    private String password;
}
