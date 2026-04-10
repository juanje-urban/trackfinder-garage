package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingUseCase;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CheckoutEventBookingRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventBookingRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-bookings")
public class EventBookingController {

    private final EventBookingUseCase eventBookingUseCase;
    private final EventBookingWebMapper eventBookingWebMapper;

    public EventBookingController(EventBookingUseCase eventBookingUseCase,
                                  EventBookingWebMapper eventBookingWebMapper) {
        this.eventBookingUseCase = eventBookingUseCase;
        this.eventBookingWebMapper = eventBookingWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventBookingResponse createEventBooking(@Valid @RequestBody CreateEventBookingRequest request) {
        EventBooking createdEventBooking = eventBookingUseCase.createEventBooking(eventBookingWebMapper.toDomain(request));
        return eventBookingWebMapper.toResponse(createdEventBooking);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public EventBookingResponse checkoutEventBooking(@Valid @RequestBody CheckoutEventBookingRequest request,
                                                     Authentication authentication) {
        EventBooking createdEventBooking = eventBookingUseCase.checkoutEventBooking(
                authentication != null ? authentication.getName() : null,
                request.getEventId(),
                request.getEventServiceIds()
        );
        return eventBookingWebMapper.toResponse(createdEventBooking);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventBooking(@PathVariable Long id, Authentication authentication) {
        eventBookingUseCase.deleteOwnEventBooking(
                authentication != null ? authentication.getName() : null,
                id
        );
    }

    @GetMapping
    public List<EventBookingResponse> getAllEventBookings() {
        return eventBookingUseCase.getAllEventBookings()
                .stream()
                .map(eventBookingWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public EventBookingResponse getEventBookingById(@PathVariable Long id) {
        return eventBookingWebMapper.toResponse(eventBookingUseCase.getEventBookingById(id));
    }

    @GetMapping("/me")
    public List<EventBookingResponse> getCurrentUserEventBookings(Authentication authentication) {
        return eventBookingUseCase.getEventBookingsByAuthenticatedEmail(
                        authentication != null ? authentication.getName() : null
                ).stream()
                .map(eventBookingWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/user/{userId}")
    public List<EventBookingResponse> getEventBookingsByUserId(@PathVariable Long userId) {
        return eventBookingUseCase.getEventBookingsByUserId(userId)
                .stream()
                .map(eventBookingWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/event/{eventId}")
    public List<EventBookingResponse> getEventBookingsByEventId(@PathVariable Long eventId) {
        return eventBookingUseCase.getEventBookingsByEventId(eventId)
                .stream()
                .map(eventBookingWebMapper::toResponse)
                .toList();
    }
}
