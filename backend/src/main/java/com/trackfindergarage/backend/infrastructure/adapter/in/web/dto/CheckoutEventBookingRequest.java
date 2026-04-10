package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CheckoutEventBookingRequest {

    @NotNull
    private Long eventId;

    private List<Long> eventServiceIds = new ArrayList<>();
}
