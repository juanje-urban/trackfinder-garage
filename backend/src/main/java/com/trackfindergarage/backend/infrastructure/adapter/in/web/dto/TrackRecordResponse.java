package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * DTO de salida con el récord o una vuelta destacada de un circuito.
 *
 * <p>Resume los datos clave de la vuelta y del piloto asociado para poder mostrar rankings o récords
 * de pista en una vista compacta.</p>
 */
@Getter
@Builder
public class TrackRecordResponse {

    private Long trackId;
    private String trackName;
    private String userDisplayName;
    private LocalDate lapDate;
    private Long lapTimeMs;
    private String vehicle;
}
