package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventBookingService;

import java.util.List;

/**
 * Define los casos de uso relacionados con los servicios adicionales contratados en una reserva.
 */
public interface EventBookingServiceUseCase {

    /**
     * Recupera los servicios contratados dentro de una reserva concreta.
     *
     * @param eventBookingId identificador de la reserva
     * @return listado de servicios contratados
     */
    List<EventBookingService> getEventBookingServicesByEventBookingId(Long eventBookingId);

    /**
     * Recupera los servicios contratados por el usuario autenticado en un evento concreto.
     *
     * @param eventId identificador del evento
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de servicios contratados por el usuario en ese evento
     */
    List<EventBookingService> getEventBookingServicesByEventIdAndAuthenticatedEmail(Long eventId, String authenticatedEmail);
}
