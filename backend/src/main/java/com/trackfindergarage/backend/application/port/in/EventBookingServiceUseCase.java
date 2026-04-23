package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventBookingService;

import java.util.List;

public interface EventBookingServiceUseCase {

    List<EventBookingService> getEventBookingServicesByEventBookingId(Long eventBookingId);

    List<EventBookingService> getEventBookingServicesByEventIdAndAuthenticatedEmail(Long eventId, String authenticatedEmail);
}
