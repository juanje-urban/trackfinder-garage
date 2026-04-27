package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventBookingServiceUseCase;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Implementa la consulta de servicios adicionales contratados dentro de las reservas.
 */
@org.springframework.stereotype.Service
@Transactional
public class EventBookingServiceService implements EventBookingServiceUseCase {

    private static final String EVENT_BOOKING_NOT_FOUND_WITH_ID = "Reserva de evento no encontrada con id: ";
    private static final String EVENT_NOT_FOUND_WITH_ID = "Evento no encontrado con id: ";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "Usuario no encontrado con correo electrónico: ";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "El correo electrónico del usuario autenticado es obligatorio";

    private final EventBookingServicePersistencePort eventBookingServicePersistencePort;
    private final EventBookingPersistencePort eventBookingPersistencePort;
    private final EventPersistencePort eventPersistencePort;
    private final UserPersistencePort userPersistencePort;

    public EventBookingServiceService(EventBookingServicePersistencePort eventBookingServicePersistencePort,
                                      EventBookingPersistencePort eventBookingPersistencePort,
                                      EventPersistencePort eventPersistencePort,
                                      UserPersistencePort userPersistencePort) {
        this.eventBookingServicePersistencePort = eventBookingServicePersistencePort;
        this.eventBookingPersistencePort = eventBookingPersistencePort;
        this.eventPersistencePort = eventPersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    /**
     * Recupera los servicios contratados en una reserva concreta.
     *
     * @param eventBookingId identificador de la reserva
     * @return listado de servicios contratados
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventBookingService> getEventBookingServicesByEventBookingId(Long eventBookingId) {
        eventBookingPersistencePort.findById(eventBookingId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_BOOKING_NOT_FOUND_WITH_ID + eventBookingId));

        return eventBookingServicePersistencePort.findByEventBookingId(eventBookingId);
    }

    /**
     * Recupera los servicios contratados por el usuario en un evento concreto.
     *
     * @param eventId identificador del evento
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de servicios contratados por el usuario en ese evento
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventBookingService> getEventBookingServicesByEventIdAndAuthenticatedEmail(Long eventId,
                                                                                            String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));

        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        Long userId = userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail))
                .getId();

        return eventBookingServicePersistencePort.findByEventBookingEventIdAndEventBookingUserId(eventId, userId);
    }
}
