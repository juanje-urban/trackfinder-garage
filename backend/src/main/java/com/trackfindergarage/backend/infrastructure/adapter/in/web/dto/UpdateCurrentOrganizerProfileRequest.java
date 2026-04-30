package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para actualizar el perfil del organizador autenticado.
 *
 * <p>Incluye tanto los datos personales de la cuenta asociada como la información legal y fiscal
 * del organizador. La contraseña es opcional para permitir cambios parciales del perfil.</p>
 */
@Getter
@Setter
public class UpdateCurrentOrganizerProfileRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String surname;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    private String address;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 255)
    private String legalName;

    @NotBlank
    @Size(max = 20)
    private String cif;

    @Size(max = 255)
    private String password;
}
