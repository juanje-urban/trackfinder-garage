package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingServiceUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventBookingServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingServiceWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventBookingServiceResponse createEventBookingService(
            @Valid @RequestBody CreateEventBookingServiceRequest request) {
        return eventBookingServiceWebMapper.toResponse(
                eventBookingServiceUseCase.createEventBookingService(eventBookingServiceWebMapper.toDomain(request))
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventBookingService(@PathVariable Long id) {
        eventBookingServiceUseCase.deleteEventBookingService(id);
    }

    @GetMapping
    public List<EventBookingServiceResponse> getAllEventBookingServices() {
        return mapResponses(
                eventBookingServiceUseCase.getAllEventBookingServices(),
                eventBookingServiceWebMapper::toResponse
        );
    }

    @GetMapping("/{id}")
    public EventBookingServiceResponse getEventBookingServiceById(@PathVariable Long id) {
        return eventBookingServiceWebMapper.toResponse(eventBookingServiceUseCase.getEventBookingServiceById(id));
    }

    @GetMapping("/booking/{eventBookingId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByEventBookingId(@PathVariable Long eventBookingId) {
        return mapResponses(
                eventBookingServiceUseCase.getEventBookingServicesByEventBookingId(eventBookingId),
                eventBookingServiceWebMapper::toResponse
        );
    }

    @GetMapping("/event/{eventId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByEventId(@PathVariable Long eventId) {
        return mapResponses(
                eventBookingServiceUseCase.getEventBookingServicesByEventId(eventId),
                eventBookingServiceWebMapper::toResponse
        );
    }

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

    @GetMapping("/user/{userId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByUserId(@PathVariable Long userId) {
        return mapResponses(
                eventBookingServiceUseCase.getEventBookingServicesByUserId(userId),
                eventBookingServiceWebMapper::toResponse
        );
    }

    @GetMapping("/event/{eventId}/user/{userId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByEventIdAndUserId(@PathVariable Long eventId,
                                                                                        @PathVariable Long userId) {
        return mapResponses(
                eventBookingServiceUseCase.getEventBookingServicesByEventIdAndUserId(eventId, userId),
                eventBookingServiceWebMapper::toResponse
        );
    }
}
