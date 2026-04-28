package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.EventService;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir los servicios ofrecidos dentro de un evento.
 *
 * <p>Permite recuperar y mantener tanto servicios derivados del catálogo del circuito como
 * servicios añadidos por el organizador para un evento concreto.</p>
 */
public interface EventServicePersistencePort {

    EventService save(EventService eventService);

    Optional<EventService> findById(Long id);

    List<EventService> findByEventId(Long eventId);

    List<EventService> findByOrganizerServiceId(Long organizerServiceId);

    Optional<EventService> findByEventIdAndTrackServiceId(Long eventId, Long trackServiceId);

    Optional<EventService> findByEventIdAndOrganizerServiceId(Long eventId, Long organizerServiceId);

    void delete(EventService eventService);
}
