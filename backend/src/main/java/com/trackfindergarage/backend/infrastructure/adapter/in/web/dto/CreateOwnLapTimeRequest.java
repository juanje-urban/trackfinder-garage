package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Datos recibidos al registrar un tiempo de vuelta para el usuario autenticado.
 */
@Getter
@Setter
public class CreateOwnLapTimeRequest {

    @NotNull
    private Long trackId;

    @NotNull
    private LocalDate lapDate;

    @NotNull
    @Positive
    private Long lapTimeMs;

    @Size(max = 30)
    private String vehicle;
}
