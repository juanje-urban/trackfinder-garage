package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingUseCase;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CheckoutEventBookingRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventBookingVisibilityRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints HTTP relacionados con el checkout y la gestión de reservas de eventos.
 */
@RestController
@RequestMapping("/event-bookings")
public class EventBookingController extends AbstractWebController {

    private final EventBookingUseCase eventBookingUseCase;
    private final EventBookingWebMapper eventBookingWebMapper;

    public EventBookingController(EventBookingUseCase eventBookingUseCase,
                                  EventBookingWebMapper eventBookingWebMapper) {
        this.eventBookingUseCase = eventBookingUseCase;
        this.eventBookingWebMapper = eventBookingWebMapper;
    }

    /**
     * Completa el checkout de una reserva para el usuario autenticado. Es la simulación del pago.
     *
     * @param request datos enviados por el cliente para la reserva
     * @param authentication autenticación del usuario actual
     * @return respuesta con la reserva creada
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public EventBookingResponse checkoutEventBooking(@Valid @RequestBody CheckoutEventBookingRequest request,
                                                     Authentication authentication) {
        return eventBookingWebMapper.toResponse(
                eventBookingUseCase.checkoutEventBooking(
                        authenticatedEmail(authentication),
                        request.getEventId(),
                        request.getEventServiceIds(),
                        Boolean.TRUE.equals(request.getVisible())
                )
        );
    }

    /**
     * Actualiza la visibilidad pública de una reserva propia.
     *
     * @param id identificador de la reserva
     * @param request nueva configuración de visibilidad
     * @param authentication autenticación del usuario actual
     * @return respuesta con la reserva actualizada
     */
    @PatchMapping("/{id}/visibility")
    public EventBookingResponse updateOwnEventBookingVisibility(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateEventBookingVisibilityRequest request,
                                                               Authentication authentication) {
        return eventBookingWebMapper.toResponse(
                eventBookingUseCase.updateOwnEventBookingVisibility(
                        authenticatedEmail(authentication),
                        id,
                        Boolean.TRUE.equals(request.getVisible())
                )
        );
    }

    /**
     * Cancela una reserva del usuario.
     *
     * @param id identificador de la reserva
     * @param authentication autenticación del usuario actual
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventBooking(@PathVariable Long id, Authentication authentication) {
        eventBookingUseCase.deleteOwnEventBooking(authenticatedEmail(authentication), id);
    }

    /**
     * Recupera las reservas del usuario.
     *
     * @param authentication autenticación del usuario actual
     * @return listado de reservas del usuario
     */
    @GetMapping("/me")
    public List<EventBookingResponse> getCurrentUserEventBookings(Authentication authentication) {
        return mapResponses(
                eventBookingUseCase.getEventBookingsByAuthenticatedEmail(authenticatedEmail(authentication)),
                eventBookingWebMapper::toResponse
        );
    }

    /**
     * Recupera las reservas visibles de un usuario concreto.
     *
     * @param userId identificador del usuario
     * @return listado de reservas visibles
     */
    @GetMapping("/user/{userId}")
    public List<EventBookingResponse> getEventBookingsByUserId(@PathVariable Long userId) {
        return mapResponses(eventBookingUseCase.getEventBookingsByUserId(userId), eventBookingWebMapper::toResponse);
    }

    /**
     * Recupera las reservas visibles asociadas a un evento.
     *
     * @param eventId identificador del evento
     * @return listado de reservas visibles del evento
     */
    @GetMapping("/event/{eventId}/visible")
    public List<EventBookingResponse> getVisibleEventBookingsByEventId(@PathVariable Long eventId) {
        return mapResponses(eventBookingUseCase.getEventBookingsByEventId(eventId)
                .stream()
                .filter(EventBooking::isVisible)
                .toList(), eventBookingWebMapper::toResponse);
    }

}
