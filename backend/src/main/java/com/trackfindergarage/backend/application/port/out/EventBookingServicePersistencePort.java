package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.EventBookingService;

import java.util.List;

public interface EventBookingServicePersistencePort {

    EventBookingService save(EventBookingService eventBookingService);

    List<EventBookingService> findByEventBookingId(Long eventBookingId);

    List<EventBookingService> findByEventBookingEventId(Long eventId);

    List<EventBookingService> findByEventBookingEventIdAndEventBookingUserId(Long eventId, Long userId);

    List<EventBookingService> findByEventServiceId(Long eventServiceId);

    void delete(EventBookingService eventBookingService);
}
