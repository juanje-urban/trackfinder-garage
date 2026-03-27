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
public class EventController {

    private final EventUseCase eventUseCase;
    private final EventWebMapper eventWebMapper;

    public EventController(EventUseCase eventUseCase, EventWebMapper eventWebMapper) {
        this.eventUseCase = eventUseCase;
        this.eventWebMapper = eventWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event createdEvent = eventUseCase.createEvent(eventWebMapper.toDomain(request));
        return toResponse(createdEvent);
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest request) {
        Event eventToUpdate = new Event();
        eventWebMapper.updateDomain(eventToUpdate, request);

        Event updatedEvent = eventUseCase.updateEvent(id, eventToUpdate);
        return toResponse(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventUseCase.deleteEvent(id);
    }

    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventUseCase.getAllEvents()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/future")
    public List<EventResponse> getFutureEvents() {
        return eventUseCase.getFutureEvents()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return toResponse(eventUseCase.getEventById(id));
    }

    @GetMapping("/organizer/{organizerId}")
    public List<EventResponse> getEventsByOrganizerId(@PathVariable Long organizerId) {
        return eventUseCase.getEventsByOrganizerId(organizerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/track/{trackId}")
    public List<EventResponse> getEventsByTrackId(@PathVariable Long trackId) {
        return eventUseCase.getEventsByTrackId(trackId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/range")
    public List<EventResponse> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return eventUseCase.getEventsByDateRange(startDate, endDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EventResponse toResponse(Event event) {
        return eventWebMapper.toResponse(event, eventUseCase.getRemainingCapacity(event.getId()));
    }
}
