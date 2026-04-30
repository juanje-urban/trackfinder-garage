package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO de salida principal del workspace del organizador.
 *
 * <p>Reúne en una sola respuesta el organizador autenticado, su catálogo disponible, sus eventos
 * gestionados y las estadísticas necesarias para pintar el panel completo.</p>
 */
@Getter
@Builder
public class OrganizerWorkspaceResponse {

    private OrganizerResponse organizer;
    private List<ServiceResponse> availableServices;
    private List<OrganizerServiceResponse> organizerServices;
    private List<TrackResponse> tracks;
    private List<TrackServiceResponse> trackServices;
    private List<OrganizerManagedEventResponse> events;
    private OrganizerWorkspaceStatsResponse stats;
}
