package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackResponse {

    private Long id;
    private String name;
    private String location;
    private String description;
}