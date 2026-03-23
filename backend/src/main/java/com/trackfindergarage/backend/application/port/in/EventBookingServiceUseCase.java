package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventBookingService;

import java.util.List;

public interface EventBookingServiceUseCase {

    EventBookingService createEventBookingService(EventBookingService eventBookingService);

    void deleteEventBookingService(Long id);

    List<EventBookingService> getAllEventBookingServices();

    EventBookingService getEventBookingServiceById(Long id);

    List<EventBookingService> getEventBookingServicesByEventBookingId(Long eventBookingId);

    List<EventBookingService> getEventBookingServicesByEventId(Long eventId);

    List<EventBookingService> getEventBookingServicesByUserId(Long userId);

    List<EventBookingService> getEventBookingServicesByEventIdAndUserId(Long eventId, Long userId);
}
