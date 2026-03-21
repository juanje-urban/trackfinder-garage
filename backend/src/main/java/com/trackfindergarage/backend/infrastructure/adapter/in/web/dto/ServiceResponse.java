package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean allowedForTrack;
    private Boolean allowedForOrganizer;
    private Boolean enabled;
}
