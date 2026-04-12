package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizerServiceResponse {

    private Long id;
    private Long organizerId;
    private String organizerLegalName;
    private Long serviceId;
    private String serviceName;
    private Boolean enabled;
}
