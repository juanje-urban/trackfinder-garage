package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventWebMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        return toResponse(eventUseCase.createEvent(eventWebMapper.toDomain(request)));
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest request) {
        Event eventToUpdate = new Event();
        eventWebMapper.updateDomain(eventToUpdate, request);
        return toResponse(eventUseCase.updateEvent(id, eventToUpdate));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventUseCase.deleteEvent(id);
    }

    @GetMapping
    public List<EventResponse> getAllEvents() {
        return mapResponses(eventUseCase.getAllEvents(), this::toResponse);
    }

    @GetMapping("/future")
    public List<EventResponse> getFutureEvents() {
        return mapResponses(eventUseCase.getFutureEvents(), this::toResponse);
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return toResponse(eventUseCase.getEventById(id));
    }

    @GetMapping("/organizer/{organizerId}")
    public List<EventResponse> getEventsByOrganizerId(@PathVariable Long organizerId) {
        return mapResponses(eventUseCase.getEventsByOrganizerId(organizerId), this::toResponse);
    }

    @GetMapping("/track/{trackId}")
    public List<EventResponse> getEventsByTrackId(@PathVariable Long trackId) {
        return mapResponses(eventUseCase.getEventsByTrackId(trackId), this::toResponse);
    }

    @GetMapping("/range")
    public List<EventResponse> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return mapResponses(eventUseCase.getEventsByDateRange(startDate, endDate), this::toResponse);
    }

    private EventResponse toResponse(Event event) {
        return eventWebMapper.toResponse(event, eventUseCase.getRemainingCapacity(event.getId()));
    }
}
