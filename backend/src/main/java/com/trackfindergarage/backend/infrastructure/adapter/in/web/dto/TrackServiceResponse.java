package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackServiceResponse {

    private Long id;
    private Long trackId;
    private String trackName;
    private Long serviceId;
    private String serviceName;
}
