package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEventBookingServiceRequest {

    @NotNull
    private Long eventBookingId;

    @NotNull
    private Long eventServiceId;
}
