package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de salida con el resumen público visible del perfil de un usuario.
 *
 * <p>Contiene sólo métricas agregadas y datos de identificación pública pensados para mostrarse a
 * otros usuarios sin exponer información privada.</p>
 */
@Getter
@Builder
public class PublicUserProfileResponse {

    private Long id;
    private String displayName;
    private long completedEvents;
    private long visitedCircuits;
    private long topFiveLapTimes;
    private long poleCount;
}
