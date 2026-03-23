package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventBookingService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEventBookingServiceRepository extends JpaRepository<EventBookingService, Long> {

    List<EventBookingService> findByEventBookingId(Long eventBookingId);

    List<EventBookingService> findByEventBookingEventId(Long eventId);

    List<EventBookingService> findByEventBookingUserId(Long userId);

    List<EventBookingService> findByEventBookingEventIdAndEventBookingUserId(Long eventId, Long userId);

    Optional<EventBookingService> findByEventBookingIdAndEventServiceId(Long eventBookingId, Long eventServiceId);
}
