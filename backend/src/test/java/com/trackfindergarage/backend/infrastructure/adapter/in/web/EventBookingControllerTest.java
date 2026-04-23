package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CheckoutEventBookingRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventBookingVisibilityRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventBookingControllerTest {

    private final EventBookingUseCase eventBookingUseCase = mock(EventBookingUseCase.class);
    private final EventBookingWebMapper eventBookingWebMapper = new EventBookingWebMapper();
    private final EventBookingController eventBookingController =
            new EventBookingController(eventBookingUseCase, eventBookingWebMapper);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void checkoutEventBookingDelegatesToUseCaseAndMapsResponse() {
        CheckoutEventBookingRequest request = new CheckoutEventBookingRequest();
        request.setEventId(5L);
        request.setEventServiceIds(List.of(11L, 12L));
        request.setVisible(true);

        when(authentication.getName()).thenReturn("driver@example.com");
        when(eventBookingUseCase.checkoutEventBooking("driver@example.com", 5L, List.of(11L, 12L), true))
                .thenReturn(booking(80L, 1L, "driver", 5L, true));

        EventBookingResponse response = eventBookingController.checkoutEventBooking(request, authentication);

        assertEquals(80L, response.getId());
        assertEquals("Jarama", response.getTrackName());
        verify(eventBookingUseCase).checkoutEventBooking("driver@example.com", 5L, List.of(11L, 12L), true);
    }

    @Test
    void updateOwnEventBookingVisibilityDelegatesToUseCaseAndMapsResponse() {
        UpdateEventBookingVisibilityRequest request = new UpdateEventBookingVisibilityRequest();
        request.setVisible(false);

        when(authentication.getName()).thenReturn("driver@example.com");
        when(eventBookingUseCase.updateOwnEventBookingVisibility("driver@example.com", 80L, false))
                .thenReturn(booking(80L, 1L, "driver", 5L, false));

        EventBookingResponse response =
                eventBookingController.updateOwnEventBookingVisibility(80L, request, authentication);

        assertEquals(false, response.isVisible());
        verify(eventBookingUseCase).updateOwnEventBookingVisibility("driver@example.com", 80L, false);
    }

    @Test
    void deleteEventBookingDelegatesToUseCase() {
        when(authentication.getName()).thenReturn("driver@example.com");

        eventBookingController.deleteEventBooking(80L, authentication);

        verify(eventBookingUseCase).deleteOwnEventBooking("driver@example.com", 80L);
    }

    @Test
    void queryEndpointsMapResponsesAndVisibleFilter() {
        when(authentication.getName()).thenReturn("driver@example.com");
        when(eventBookingUseCase.getEventBookingsByAuthenticatedEmail("driver@example.com"))
                .thenReturn(List.of(booking(80L, 1L, "driver", 5L, true)));
        when(eventBookingUseCase.getEventBookingsByUserId(2L))
                .thenReturn(List.of(booking(81L, 2L, "other", 5L, true)));
        when(eventBookingUseCase.getEventBookingsByEventId(5L))
                .thenReturn(List.of(booking(82L, 3L, "visible", 5L, true), booking(83L, 4L, "hidden", 5L, false)));

        List<EventBookingResponse> ownResponses = eventBookingController.getCurrentUserEventBookings(authentication);
        List<EventBookingResponse> userResponses = eventBookingController.getEventBookingsByUserId(2L);
        List<EventBookingResponse> visibleResponses = eventBookingController.getVisibleEventBookingsByEventId(5L);

        assertEquals(1, ownResponses.size());
        assertEquals("driver", ownResponses.get(0).getUserDisplayName());
        assertEquals(1, userResponses.size());
        assertEquals(2L, userResponses.get(0).getUserId());
        assertEquals(1, visibleResponses.size());
        assertEquals(82L, visibleResponses.get(0).getId());
    }

    private EventBooking booking(Long id, Long userId, String displayName, Long eventId, boolean visible) {
        User user = new User();
        user.setId(userId);
        user.setDisplayName(displayName);

        Track track = new Track();
        track.setId(7L);
        track.setName("Jarama");

        Organizer organizer = new Organizer();
        organizer.setIdUser(3L);
        organizer.setLegalName("Track Events");

        Event event = new Event();
        event.setId(eventId);
        event.setTrack(track);
        event.setOrganizer(organizer);
        event.setEventDate(LocalDate.of(2026, 5, 10));

        EventBooking booking = new EventBooking();
        booking.setId(id);
        booking.setUser(user);
        booking.setEvent(event);
        booking.setBookedAt(LocalDateTime.of(2026, 4, 10, 10, 0));
        booking.setBasePriceAtPurchase(new BigDecimal("120.00"));
        booking.setVisible(visible);
        return booking;
    }
}
