package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrganizerManagedEventResponse {

    private EventResponse event;
    private List<EventServiceResponse> services;
    private OrganizerWorkspaceEventStatsResponse stats;
}
