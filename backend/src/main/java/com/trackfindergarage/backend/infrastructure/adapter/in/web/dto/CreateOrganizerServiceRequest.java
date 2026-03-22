package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrganizerServiceRequest {

    @NotNull
    private Long organizerId;

    @NotNull
    private Long serviceId;
}
