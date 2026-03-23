package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventBooking;

import java.util.List;

public interface EventBookingUseCase {

    EventBooking createEventBooking(EventBooking eventBooking);

    void deleteEventBooking(Long id);

    List<EventBooking> getAllEventBookings();

    EventBooking getEventBookingById(Long id);

    List<EventBooking> getEventBookingsByUserId(Long userId);

    List<EventBooking> getEventBookingsByEventId(Long eventId);
}
