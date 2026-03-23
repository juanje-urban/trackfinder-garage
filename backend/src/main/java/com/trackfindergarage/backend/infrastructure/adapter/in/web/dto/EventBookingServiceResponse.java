package com.trackfindergarage.backend.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EventBookingServiceResponse {

    private Long id;
    private Long eventBookingId;
    private Long userId;
    private Long eventId;
    private Long eventServiceId;
    private BigDecimal priceAtPurchase;
}
