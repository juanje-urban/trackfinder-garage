package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.EventBookingService;

import java.util.List;

/**
 * Puerto de salida para persistir servicios extra contratados dentro de una reserva.
 *
 * <p>Permite consultar y mantener las líneas de detalle asociadas a una reserva concreta o a un
 * servicio de evento determinado.</p>
 */
public interface EventBookingServicePersistencePort {

    EventBookingService save(EventBookingService eventBookingService);

    List<EventBookingService> findByEventBookingId(Long eventBookingId);

    List<EventBookingService> findByEventBookingEventId(Long eventId);

    List<EventBookingService> findByEventBookingEventIdAndEventBookingUserId(Long eventId, Long userId);

    List<EventBookingService> findByEventServiceId(Long eventServiceId);

    void delete(EventBookingService eventBookingService);
}
