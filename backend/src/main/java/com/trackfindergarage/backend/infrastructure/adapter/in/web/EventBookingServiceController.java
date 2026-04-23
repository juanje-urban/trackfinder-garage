package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingServiceUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingServiceWebMapper;
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

    @GetMapping("/booking/{eventBookingId}")
    public List<EventBookingServiceResponse> getEventBookingServicesByEventBookingId(@PathVariable Long eventBookingId) {
        return mapResponses(
                eventBookingServiceUseCase.getEventBookingServicesByEventBookingId(eventBookingId),
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

}
