package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventServiceUseCase;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventServiceWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventServiceResponse createEventService(@Valid @RequestBody CreateEventServiceRequest request) {
        return eventServiceWebMapper.toResponse(eventServiceUseCase.createEventService(eventServiceWebMapper.toDomain(request)));
    }

    @PutMapping("/{id}")
    public EventServiceResponse updateEventService(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateEventServiceRequest request) {
        EventService eventServiceToUpdate = new EventService();
        eventServiceWebMapper.updateDomain(eventServiceToUpdate, request);
        return eventServiceWebMapper.toResponse(eventServiceUseCase.updateEventService(id, eventServiceToUpdate));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventService(@PathVariable Long id) {
        eventServiceUseCase.deleteEventService(id);
    }

    @GetMapping
    public List<EventServiceResponse> getAllEventServices() {
        return mapResponses(eventServiceUseCase.getAllEventServices(), eventServiceWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    public EventServiceResponse getEventServiceById(@PathVariable Long id) {
        return eventServiceWebMapper.toResponse(eventServiceUseCase.getEventServiceById(id));
    }

    @GetMapping("/event/{eventId}")
    public List<EventServiceResponse> getEventServicesByEventId(@PathVariable Long eventId) {
        return mapResponses(eventServiceUseCase.getEventServicesByEventId(eventId), eventServiceWebMapper::toResponse);
    }
}
