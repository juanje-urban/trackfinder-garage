package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para actualizar el perfil del usuario autenticado.
 *
 * <p>Permite modificar los datos de contacto y, opcionalmente, cambiar la contraseña de acceso sin
 * obligar a reenviarla en cada actualización.</p>
 */
@Getter
@Setter
public class UpdateCurrentUserProfileRequest {

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

    @Size(max = 255)
    private String password;
}
