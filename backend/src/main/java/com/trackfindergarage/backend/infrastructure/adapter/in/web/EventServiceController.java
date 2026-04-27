package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventServiceUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventServiceWebMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints HTTP de consulta de servicios adicionales asociados a un evento.
 */
@RestController
@RequestMapping("/event-services")
public class EventServiceController extends AbstractWebController {

    private final EventServiceUseCase eventServiceUseCase;
    private final EventServiceWebMapper eventServiceWebMapper;

    public EventServiceController(EventServiceUseCase eventServiceUseCase,
                                  EventServiceWebMapper eventServiceWebMapper) {
        this.eventServiceUseCase = eventServiceUseCase;
        this.eventServiceWebMapper = eventServiceWebMapper;
    }

    /**
     * Recupera los servicios configurados para un evento concreto.
     *
     * @param eventId identificador del evento
     * @return listado de servicios del evento
     */
    @GetMapping("/event/{eventId}")
    public List<EventServiceResponse> getEventServicesByEventId(@PathVariable Long eventId) {
        return mapResponses(eventServiceUseCase.getEventServicesByEventId(eventId), eventServiceWebMapper::toResponse);
    }
}
