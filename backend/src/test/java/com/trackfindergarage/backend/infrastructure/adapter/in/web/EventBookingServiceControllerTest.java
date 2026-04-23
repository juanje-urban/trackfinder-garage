package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingServiceUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingServiceWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventBookingServiceControllerTest {

    private final EventBookingServiceUseCase eventBookingServiceUseCase = mock(EventBookingServiceUseCase.class);
    private final EventBookingServiceWebMapper eventBookingServiceWebMapper = new EventBookingServiceWebMapper();
    private final EventBookingServiceController eventBookingServiceController =
            new EventBookingServiceController(eventBookingServiceUseCase, eventBookingServiceWebMapper);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void getEventBookingServicesByEventBookingIdMapsResponses() {
        when(eventBookingServiceUseCase.getEventBookingServicesByEventBookingId(7L))
                .thenReturn(List.of(eventBookingService(11L, 7L, 4L, 9L)));

        List<EventBookingServiceResponse> responses =
                eventBookingServiceController.getEventBookingServicesByEventBookingId(7L);

        assertEquals(1, responses.size());
        assertEquals(7L, responses.get(0).getEventBookingId());
        assertEquals(9L, responses.get(0).getEventServiceId());
    }

    @Test
    void getCurrentUserEventBookingServicesByEventIdDelegatesToUseCase() {
        when(authentication.getName()).thenReturn("driver@example.com");
        when(eventBookingServiceUseCase.getEventBookingServicesByEventIdAndAuthenticatedEmail(4L, "driver@example.com"))
                .thenReturn(List.of(eventBookingService(11L, 7L, 4L, 9L)));

        List<EventBookingServiceResponse> responses =
                eventBookingServiceController.getCurrentUserEventBookingServicesByEventId(4L, authentication);

        assertEquals(1, responses.size());
        assertEquals(4L, responses.get(0).getEventId());
        verify(eventBookingServiceUseCase).getEventBookingServicesByEventIdAndAuthenticatedEmail(4L, "driver@example.com");
    }

    private EventBookingService eventBookingService(Long id, Long bookingId, Long eventId, Long eventServiceId) {
        User user = new User();
        user.setId(3L);

        Event event = new Event();
        event.setId(eventId);

        EventBooking eventBooking = new EventBooking();
        eventBooking.setId(bookingId);
        eventBooking.setUser(user);
        eventBooking.setEvent(event);

        EventService service = new EventService();
        service.setId(eventServiceId);

        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setId(id);
        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(service);
        eventBookingService.setPriceAtPurchase(new BigDecimal("25.00"));
        return eventBookingService;
    }
}
