package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrganizerEventServiceInput {

    private Long trackServiceId;

    private Long organizerServiceId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}
