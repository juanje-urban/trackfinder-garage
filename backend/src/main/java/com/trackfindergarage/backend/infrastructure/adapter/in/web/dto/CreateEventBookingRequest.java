package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEventBookingRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long eventId;

    @NotNull
    @JsonProperty("isVisible")
    private Boolean visible;
}
