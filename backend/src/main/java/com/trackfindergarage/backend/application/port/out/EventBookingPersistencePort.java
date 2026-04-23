package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.EventBooking;

import java.util.List;
import java.util.Optional;

public interface EventBookingPersistencePort {

    EventBooking save(EventBooking eventBooking);

    Optional<EventBooking> findById(Long id);

    List<EventBooking> findByUserId(Long userId);

    List<EventBooking> findByEventId(Long eventId);

    long countByEventId(Long eventId);

    Optional<EventBooking> findByUserIdAndEventId(Long userId, Long eventId);

    void delete(EventBooking eventBooking);
}
