package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingServiceUseCase;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventBookingServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingServiceWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-booking-services")
public class EventBookingServiceController {

    private final EventBookingServiceUseCase eventBookingServiceUseCase;
    private final EventBookingServiceWebMapper eventBookingServiceWebMapper;

    public EventBookingServiceController(EventBookingServiceUseCase eventBookingServiceUseCase,
                                         EventBookingServiceWebMapper eventBookingServiceWebMapper) {
        this.eventBookingServiceUseCase = eventBookingServiceUseCase;
        this.eventBookingServiceWebMapper = eventBookingServiceWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventBookingServiceResponse createEventBookingService(
            @Valid @RequestBody CreateEventBookingServiceRequest request) {
        EventBookingService createdEventBookingService =
                eventBookingServiceUseCase.createEventBookingService(eventBookingServiceWebMapper.toDomain(request));
        return eventBookingServiceWebMapper.toResponse(createdEventBookingService);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventBookingService(@PathVariable Long id) {
        eventBookingServiceUseCase.deleteEventBookingService(id);
    }

    @GetMapping
    public List<EventBookingServiceResponse> getAllEventBookingServices() {
        return eventBookingServiceUseCase.getAllEventBookingServices()
                .stream()
                .map(eventBookingServiceWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public EventBookingServiceResponse getEventBookingServiceById(@PathVariable Long id) {
        return eventBookingServiceWebMapper.toResponse(eventBookingServiceUseCase.getEventBookingServiceById(id));
    }

    @GetMapping("/booking/{eventBookingId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByEventBookingId(@PathVariable Long eventBookingId) {
        return eventBookingServiceUseCase.getEventBookingServicesByEventBookingId(eventBookingId)
                .stream()
                .map(eventBookingServiceWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/event/{eventId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByEventId(@PathVariable Long eventId) {
        return eventBookingServiceUseCase.getEventBookingServicesByEventId(eventId)
                .stream()
                .map(eventBookingServiceWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/user/{userId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByUserId(@PathVariable Long userId) {
        return eventBookingServiceUseCase.getEventBookingServicesByUserId(userId)
                .stream()
                .map(eventBookingServiceWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/event/{eventId}/user/{userId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByEventIdAndUserId(@PathVariable Long eventId,
                                                                                        @PathVariable Long userId) {
        return eventBookingServiceUseCase.getEventBookingServicesByEventIdAndUserId(eventId, userId)
                .stream()
                .map(eventBookingServiceWebMapper::toResponse)
                .toList();
    }
}
