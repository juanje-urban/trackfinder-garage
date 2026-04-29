package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.EventBooking;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar reservas de eventos.
 *
 * <p>Da acceso a las operaciones necesarias para gestionar inscripciones, validar duplicados y
 * calcular ocupación.</p>
 */
public interface EventBookingPersistencePort {

    EventBooking save(EventBooking eventBooking);

    Optional<EventBooking> findById(Long id);

    List<EventBooking> findByUserId(Long userId);

    List<EventBooking> findByEventId(Long eventId);

    long countByEventId(Long eventId);

    Optional<EventBooking> findByUserIdAndEventId(Long userId, Long eventId);

    void delete(EventBooking eventBooking);
}
