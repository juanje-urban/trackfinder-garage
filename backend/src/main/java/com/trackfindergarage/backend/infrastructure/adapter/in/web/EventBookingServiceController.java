package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingServiceUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingServiceWebMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints HTTP de consulta de servicios adicionales contratados en reservas.
 */
@RestController
@RequestMapping("/event-booking-services")
public class EventBookingServiceController extends AbstractWebController {

    private final EventBookingServiceUseCase eventBookingServiceUseCase;
    private final EventBookingServiceWebMapper eventBookingServiceWebMapper;

    public EventBookingServiceController(EventBookingServiceUseCase eventBookingServiceUseCase,
                                         EventBookingServiceWebMapper eventBookingServiceWebMapper) {
        this.eventBookingServiceUseCase = eventBookingServiceUseCase;
        this.eventBookingServiceWebMapper = eventBookingServiceWebMapper;
    }

    /**
     * Recupera los servicios contratados en una reserva concreta.
     *
     * @param eventBookingId identificador de la reserva
     * @return listado de servicios contratados
     */
    @GetMapping("/booking/{eventBookingId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByEventBookingId(@PathVariable Long eventBookingId) {
        return mapResponses(
                eventBookingServiceUseCase.getEventBookingServicesByEventBookingId(eventBookingId),
                eventBookingServiceWebMapper::toResponse
        );
    }

    /**
     * Recupera los servicios contratados por el usuario autenticado en un evento concreto.
     *
     * @param eventId identificador del evento
     * @param authentication autenticación del usuario actual
     * @return listado de servicios contratados por el usuario en ese evento
     */
    @GetMapping("/event/{eventId}/me")
    public List<EventBookingServiceResponse> getCurrentUserEventBookingServicesByEventId(@PathVariable Long eventId,
                                                                                         Authentication authentication) {
        return mapResponses(
                eventBookingServiceUseCase.getEventBookingServicesByEventIdAndAuthenticatedEmail(
                        eventId,
                        authenticatedEmail(authentication)
                ),
                eventBookingServiceWebMapper::toResponse
        );
    }

}
