package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para el registro de usuarios finales.
 */
@Getter
@Setter
public class AuthRegisterRequest {

    /**
     * Alias público del usuario.
     */
    @NotBlank
    @Size(max = 255)
    private String displayName;

    /**
     * Email único que se utilizara para autenticarse.
     */
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    /**
     * Contraseña en texto plano proporcionada durante el alta.
     */
    @NotBlank
    @Size(max = 255)
    private String password;

    /**
     * Nombre real del usuario.
     */
    @NotBlank
    @Size(max = 255)
    private String name;

    /**
     * Apellidos del usuario.
     */
    @NotBlank
    @Size(max = 255)
    private String surname;

    /**
     * Dirección postal del usuario.
     */
    @NotBlank
    @Size(max = 255)
    private String address;

    /**
     * Teléfono de contacto del usuario.
     */
    @NotBlank
    @Size(max = 20)
    private String phone;
}
