package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventBooking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para la entidad {@link EventBooking}.
 *
 * <p>Declara las consultas de reservas e hidrata junto a cada una sus referencias principales para
 * que la aplicación pueda trabajar con la reserva completa.</p>
 */
public interface SpringDataEventBookingRepository extends JpaRepository<EventBooking, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "event", "event.organizer", "event.track"})
    Optional<EventBooking> findById(Long id);

    @EntityGraph(attributePaths = {"user", "event", "event.organizer", "event.track"})
    List<EventBooking> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "event", "event.organizer", "event.track"})
    List<EventBooking> findByEventId(Long eventId);

    long countByEventId(Long eventId);

    @EntityGraph(attributePaths = {"user", "event", "event.organizer", "event.track"})
    Optional<EventBooking> findByUserIdAndEventId(Long userId, Long eventId);
}
