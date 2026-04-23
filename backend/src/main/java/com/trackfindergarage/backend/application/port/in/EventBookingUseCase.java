package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventBooking;

import java.util.List;

public interface EventBookingUseCase {

    EventBooking checkoutEventBooking(String authenticatedEmail, Long eventId, List<Long> eventServiceIds, boolean isVisible);

    EventBooking updateOwnEventBookingVisibility(String authenticatedEmail, Long id, boolean isVisible);

    void deleteOwnEventBooking(String authenticatedEmail, Long id);

    List<EventBooking> getEventBookingsByAuthenticatedEmail(String authenticatedEmail);

    List<EventBooking> getEventBookingsByUserId(Long userId);

    List<EventBooking> getEventBookingsByEventId(Long eventId);
}
