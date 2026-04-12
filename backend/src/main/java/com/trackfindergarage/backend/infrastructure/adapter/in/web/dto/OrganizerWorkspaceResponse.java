package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
