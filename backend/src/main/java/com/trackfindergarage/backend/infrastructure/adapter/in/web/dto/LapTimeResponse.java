package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class LapTimeResponse {

    private Long id;
    private Long userId;
    private String userDisplayName;
    private Long trackId;
    private String trackName;
    private LocalDate lapDate;
    private Long lapTimeMs;
    private String vehicle;
}
