package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EventServiceResponse {

    private Long id;
    private Long eventId;
    private Long trackServiceId;
    private String trackServiceName;
    private Long organizerServiceId;
    private String organizerServiceName;
    private BigDecimal price;
    private Boolean hasBookings;
}
