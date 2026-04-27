package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventWebMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints HTTP de consulta del catálogo de eventos.
 *
 * <p>Ofrece el listado de eventos futuros y el detalle de un evento con su capacidad disponible.</p>
 */
@RestController
@RequestMapping("/events")
public class EventController extends AbstractWebController {

    private final EventUseCase eventUseCase;
    private final EventWebMapper eventWebMapper;

    public EventController(EventUseCase eventUseCase, EventWebMapper eventWebMapper) {
        this.eventUseCase = eventUseCase;
        this.eventWebMapper = eventWebMapper;
    }

    /**
     * Recupera los eventos futuros disponibles para el catálogo.
     *
     * @return listado de eventos
     */
    @GetMapping("/future")
    public List<EventResponse> getFutureEvents() {
        return mapResponses(eventUseCase.getFutureEvents(), this::toResponse);
    }

    /**
     * Recupera el detalle de un evento concreto.
     *
     * @param id identificador del evento
     * @return respuesta con el detalle del evento
     */
    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return toResponse(eventUseCase.getEventById(id));
    }

    private EventResponse toResponse(Event event) {
        return eventWebMapper.toResponse(event, eventUseCase.getRemainingCapacity(event.getId()));
    }
}
