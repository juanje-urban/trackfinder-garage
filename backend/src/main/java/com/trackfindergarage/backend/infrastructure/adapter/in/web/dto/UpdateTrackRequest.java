package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de entrada para actualizar un circuito existente del catálogo.
 *
 * <p>Permite editar tanto la información visible del circuito como su identificador corto usado por
 * el frontend para resolver recursos estáticos asociados.</p>
 */
@Getter
@Setter
public class UpdateTrackRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 120)
    @Pattern(regexp = "^[a-z0-9_]+$")
    private String shortName;

    @NotBlank
    @Size(max = 255)
    private String location;

    @NotBlank
    @Size(max = 500)
    private String description;
}
