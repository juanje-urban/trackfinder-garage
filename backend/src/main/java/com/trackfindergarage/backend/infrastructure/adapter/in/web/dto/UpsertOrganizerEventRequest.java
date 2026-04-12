package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UpsertOrganizerEventRequest {

    @NotNull
    private Long trackId;

    @NotNull
    private LocalDate eventDate;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal basePrice;

    @NotNull
    @Min(1)
    private Integer maxParticipants;

    @NotBlank
    private String description;

    @Valid
    @NotNull
    private List<OrganizerEventServiceInput> services = new ArrayList<>();
}
