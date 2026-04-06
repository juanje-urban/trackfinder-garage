package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TrackRecordResponse {

    private Long trackId;
    private String trackName;
    private String userDisplayName;
    private LocalDate lapDate;
    private Long lapTimeMs;
    private String vehicle;
}
