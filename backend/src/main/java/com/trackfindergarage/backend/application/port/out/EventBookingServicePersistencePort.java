package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.EventBookingService;

import java.util.List;
import java.util.Optional;

public interface EventBookingServicePersistencePort {

    EventBookingService save(EventBookingService eventBookingService);

    Optional<EventBookingService> findById(Long id);

    List<EventBookingService> findAll();

    List<EventBookingService> findByEventBookingId(Long eventBookingId);

    List<EventBookingService> findByEventBookingEventId(Long eventId);

    List<EventBookingService> findByEventBookingUserId(Long userId);

    List<EventBookingService> findByEventBookingEventIdAndEventBookingUserId(Long eventId, Long userId);

    List<EventBookingService> findByEventServiceId(Long eventServiceId);

    Optional<EventBookingService> findByEventBookingIdAndEventServiceId(Long eventBookingId, Long eventServiceId);

    void delete(EventBookingService eventBookingService);
}
