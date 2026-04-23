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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventBooking(@PathVariable Long id, Authentication authentication) {
        eventBookingUseCase.deleteOwnEventBooking(authenticatedEmail(authentication), id);
    }

    @GetMapping("/me")
    public List<EventBookingResponse> getCurrentUserEventBookings(Authentication authentication) {
        return mapResponses(
                eventBookingUseCase.getEventBookingsByAuthenticatedEmail(authenticatedEmail(authentication)),
                eventBookingWebMapper::toResponse
        );
    }

    @GetMapping("/user/{userId}")
    public List<EventBookingResponse> getEventBookingsByUserId(@PathVariable Long userId) {
        return mapResponses(eventBookingUseCase.getEventBookingsByUserId(userId), eventBookingWebMapper::toResponse);
    }

    @GetMapping("/event/{eventId}/visible")
    public List<EventBookingResponse> getVisibleEventBookingsByEventId(@PathVariable Long eventId) {
        return mapResponses(eventBookingUseCase.getEventBookingsByEventId(eventId)
                .stream()
                .filter(EventBooking::isVisible)
                .toList(), eventBookingWebMapper::toResponse);
    }

}
