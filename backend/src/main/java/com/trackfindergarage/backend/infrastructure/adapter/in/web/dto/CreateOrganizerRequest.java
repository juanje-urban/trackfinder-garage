package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para el registro de organizadores.
 *
 * <p>Incluye tanto los datos comunes con el usuario de rol USER como la información fiscal y legal propia de la
 * entidad organizadora.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateOrganizerRequest {

    /**
     * Alias público del usuario asociado al organizador.
     */
    @NotBlank
    @Size(max = 255)
    private String displayName;

    /**
     * Contraseña inicial del usuario asociado al organizador.
     */
    @NotBlank
    @Size(max = 255)
    private String password;

    /**
     * Email de acceso del usuario organizador.
     */
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    /**
     * Nombre real de la persona asociada a la cuenta.
     */
    @NotBlank
    @Size(max = 255)
    private String name;

    /**
     * Apellidos de la persona asociada a la cuenta.
     */
    @NotBlank
    @Size(max = 255)
    private String surname;

    /**
     * Dirección postal del organizador.
     */
    @NotBlank
    @Size(max = 255)
    private String address;

    /**
     * Teléfono de contacto del organizador.
     */
    @NotBlank
    @Size(max = 20)
    private String phone;

    /**
     * Razón social o nombre legal de la entidad organizadora.
     */
    @NotBlank
    @Size(max = 255)
    private String legalName;

    /**
     * Identificador fiscal de la entidad organizadora.
     */
    @NotBlank
    @Size(max = 20)
    private String cif;
}
