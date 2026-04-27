package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventBooking;

import java.util.List;

/**
 * Define los casos de uso relacionados con las reservas de eventos.
 */
public interface EventBookingUseCase {

    /**
     * Completa el proceso de reserva de un evento para el usuario.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param eventId identificador del evento reservado
     * @param eventServiceIds identificadores de los servicios adicionales seleccionados
     * @param isVisible indica si la reserva será visible en el perfil público
     * @return reserva creada
     */
    EventBooking checkoutEventBooking(String authenticatedEmail, Long eventId, List<Long> eventServiceIds, boolean isVisible);

    /**
     * Actualiza la visibilidad pública de una reserva propia.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param id identificador de la reserva
     * @param isVisible nuevo estado de visibilidad
     * @return reserva actualizada
     */
    EventBooking updateOwnEventBookingVisibility(String authenticatedEmail, Long id, boolean isVisible);

    /**
     * Cancela una reserva propia.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param id identificador de la reserva
     */
    void deleteOwnEventBooking(String authenticatedEmail, Long id);

    /**
     * Recupera las reservas del usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de reservas del usuario
     */
    List<EventBooking> getEventBookingsByAuthenticatedEmail(String authenticatedEmail);

    /**
     * Recupera las reservas visibles de un usuario concreto.
     *
     * @param userId identificador del usuario
     * @return listado de reservas visibles
     */
    List<EventBooking> getEventBookingsByUserId(Long userId);

    /**
     * Recupera las reservas asociadas a un evento.
     *
     * @param eventId identificador del evento
     * @return listado de reservas del evento
     */
    List<EventBooking> getEventBookingsByEventId(Long eventId);
}
