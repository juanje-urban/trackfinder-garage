package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventBookingService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link EventBookingService}.
 *
 * <p>Expone consultas sobre los servicios adicionales contratados en reservas y sus relaciones con
 * eventos y usuarios.</p>
 */
public interface SpringDataEventBookingServiceRepository extends JpaRepository<EventBookingService, Long> {

    List<EventBookingService> findByEventBookingId(Long eventBookingId);

    List<EventBookingService> findByEventBookingEventId(Long eventId);

    List<EventBookingService> findByEventBookingEventIdAndEventBookingUserId(Long eventId, Long userId);

    List<EventBookingService> findByEventServiceId(Long eventServiceId);

    Optional<EventBookingService> findByEventBookingIdAndEventServiceId(Long eventBookingId, Long eventServiceId);
}
