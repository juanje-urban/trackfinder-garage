package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventWebMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController extends AbstractWebController {

    private final EventUseCase eventUseCase;
    private final EventWebMapper eventWebMapper;

    public EventController(EventUseCase eventUseCase, EventWebMapper eventWebMapper) {
        this.eventUseCase = eventUseCase;
        this.eventWebMapper = eventWebMapper;
    }

    @GetMapping("/future")
    public List<EventResponse> getFutureEvents() {
        return mapResponses(eventUseCase.getFutureEvents(), this::toResponse);
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return toResponse(eventUseCase.getEventById(id));
    }

    private EventResponse toResponse(Event event) {
        return eventWebMapper.toResponse(event, eventUseCase.getRemainingCapacity(event.getId()));
    }
}
