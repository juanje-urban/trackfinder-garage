package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateEventRequest {

    @NotNull
    private Long organizerId;

    @NotNull
    private Long trackId;

    @NotNull
    private LocalDate eventDate;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal basePrice;
}
