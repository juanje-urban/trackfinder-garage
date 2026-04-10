package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventBooking;

import java.util.List;

public interface EventBookingUseCase {

    EventBooking createEventBooking(EventBooking eventBooking);

    EventBooking checkoutEventBooking(String authenticatedEmail, Long eventId, List<Long> eventServiceIds);

    void deleteOwnEventBooking(String authenticatedEmail, Long id);

    void deleteEventBooking(Long id);

    List<EventBooking> getAllEventBookings();

    EventBooking getEventBookingById(Long id);

    List<EventBooking> getEventBookingsByAuthenticatedEmail(String authenticatedEmail);

    List<EventBooking> getEventBookingsByUserId(Long userId);

    List<EventBooking> getEventBookingsByEventId(Long eventId);
}
