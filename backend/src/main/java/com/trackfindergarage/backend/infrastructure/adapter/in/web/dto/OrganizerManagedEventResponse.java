package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO de salida con la representación completa de un evento gestionado por un organizador.
 *
 * <p>Agrupa el evento, sus servicios configurados y sus estadísticas operativas para que el
 * frontend del workspace pueda renderizarlo sin llamadas adicionales.</p>
 */
@Getter
@Builder
public class OrganizerManagedEventResponse {

    private EventResponse event;
    private List<EventServiceResponse> services;
    private OrganizerWorkspaceEventStatsResponse stats;
}
