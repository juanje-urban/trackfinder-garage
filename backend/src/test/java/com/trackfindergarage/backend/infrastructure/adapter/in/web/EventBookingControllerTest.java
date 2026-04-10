package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CheckoutEventBookingRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventBookingRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventBookingControllerTest {

    private final EventBookingUseCase eventBookingUseCase = mock(EventBookingUseCase.class);
    private final EventBookingWebMapper eventBookingWebMapper = new EventBookingWebMapper();
    private final EventBookingController eventBookingController =
            new EventBookingController(eventBookingUseCase, eventBookingWebMapper);

    @Test
    void createEventBookingDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateEventBookingRequest request = new CreateEventBookingRequest();
        request.setUserId(1L);
        request.setEventId(2L);

        EventBooking eventBooking = eventBookingWithId(10L, 1L, 2L);

        when(eventBookingUseCase.createEventBooking(any(EventBooking.class))).thenReturn(eventBooking);

        assertEquals(10L, eventBookingController.createEventBooking(request).getId());
    }

    @Test
    void checkoutEventBookingDelegatesToUseCaseAndReturnsMappedResponse() {
        CheckoutEventBookingRequest request = new CheckoutEventBookingRequest();
        request.setEventId(2L);
        request.setEventServiceIds(List.of(9L, 10L));
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "secret");
        EventBooking eventBooking = eventBookingWithId(10L, 1L, 2L);

        when(eventBookingUseCase.checkoutEventBooking("user@example.com", 2L, List.of(9L, 10L)))
                .thenReturn(eventBooking);

        assertEquals(10L, eventBookingController.checkoutEventBooking(request, authentication).getId());
    }

    @Test
    void queryEndpointsMapUseCaseResult() {
        EventBooking eventBooking = eventBookingWithId(10L, 1L, 2L);

        when(eventBookingUseCase.getAllEventBookings()).thenReturn(List.of(eventBooking));
        when(eventBookingUseCase.getEventBookingById(10L)).thenReturn(eventBooking);
        when(eventBookingUseCase.getEventBookingsByUserId(1L)).thenReturn(List.of(eventBooking));
        when(eventBookingUseCase.getEventBookingsByEventId(2L)).thenReturn(List.of(eventBooking));

        assertEquals(1, eventBookingController.getAllEventBookings().size());
        assertEquals(10L, eventBookingController.getEventBookingById(10L).getId());
        assertEquals(1, eventBookingController.getEventBookingsByUserId(1L).size());
        assertEquals(1, eventBookingController.getEventBookingsByEventId(2L).size());
    }

    @Test
    void deleteEventBookingDelegatesToUseCase() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "secret");

        eventBookingController.deleteEventBooking(10L, authentication);

        verify(eventBookingUseCase).deleteOwnEventBooking("user@example.com", 10L);
    }

    private EventBooking eventBookingWithId(Long id, Long userId, Long eventId) {
        User user = new User();
        user.setId(userId);
        user.setDisplayName("user");

        Organizer organizer = new Organizer();
        organizer.setIdUser(3L);
        organizer.setLegalName("Organizer");

        Track track = new Track();
        track.setId(4L);
        track.setName("Track");

        Event event = new Event();
        event.setId(eventId);
        event.setEventDate(LocalDate.now().plusDays(20));
        event.setBasePrice(new BigDecimal("30.00"));
        event.setOrganizer(organizer);
        event.setTrack(track);

        EventBooking eventBooking = new EventBooking();
        eventBooking.setId(id);
        eventBooking.setUser(user);
        eventBooking.setEvent(event);
        eventBooking.setBookedAt(LocalDateTime.of(2026, 3, 23, 10, 0));
        eventBooking.setBasePriceAtPurchase(new BigDecimal("30.00"));
        return eventBooking;
    }
}
