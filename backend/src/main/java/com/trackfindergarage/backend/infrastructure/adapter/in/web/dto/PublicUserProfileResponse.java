package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

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
