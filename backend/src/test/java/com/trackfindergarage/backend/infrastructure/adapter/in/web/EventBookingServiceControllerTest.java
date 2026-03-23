package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventBookingServiceUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventBookingServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventBookingServiceWebMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventBookingServiceControllerTest {

    private final EventBookingServiceUseCase eventBookingServiceUseCase = mock(EventBookingServiceUseCase.class);
    private final EventBookingServiceWebMapper eventBookingServiceWebMapper = new EventBookingServiceWebMapper();
    private final EventBookingServiceController eventBookingServiceController =
            new EventBookingServiceController(eventBookingServiceUseCase, eventBookingServiceWebMapper);

    @Test
    void createEventBookingServiceDelegatesToUseCaseAndReturnsMappedResponse() {
        CreateEventBookingServiceRequest request = new CreateEventBookingServiceRequest();
        request.setEventBookingId(1L);
        request.setEventServiceId(2L);

        EventBookingService eventBookingService = eventBookingServiceWithId(10L, 1L, 7L, 3L, 2L);

        when(eventBookingServiceUseCase.createEventBookingService(any(EventBookingService.class))).thenReturn(eventBookingService);

        assertEquals(10L, eventBookingServiceController.createEventBookingService(request).getId());
    }

    @Test
    void queryEndpointsMapUseCaseResult() {
        EventBookingService eventBookingService = eventBookingServiceWithId(10L, 1L, 7L, 3L, 2L);

        when(eventBookingServiceUseCase.getAllEventBookingServices()).thenReturn(List.of(eventBookingService));
        when(eventBookingServiceUseCase.getEventBookingServiceById(10L)).thenReturn(eventBookingService);
        when(eventBookingServiceUseCase.getEventBookingServicesByEventBookingId(1L)).thenReturn(List.of(eventBookingService));
        when(eventBookingServiceUseCase.getEventBookingServicesByEventId(3L)).thenReturn(List.of(eventBookingService));
        when(eventBookingServiceUseCase.getEventBookingServicesByUserId(7L)).thenReturn(List.of(eventBookingService));
        when(eventBookingServiceUseCase.getEventBookingServicesByEventIdAndUserId(3L, 7L)).thenReturn(List.of(eventBookingService));

        assertEquals(1, eventBookingServiceController.getAllEventBookingServices().size());
        assertEquals(10L, eventBookingServiceController.getEventBookingServiceById(10L).getId());
        assertEquals(1, eventBookingServiceController.getEventBookingServicesByEventBookingId(1L).size());
        assertEquals(1, eventBookingServiceController.getEventBookingServicesByEventId(3L).size());
        assertEquals(1, eventBookingServiceController.getEventBookingServicesByUserId(7L).size());
        assertEquals(1, eventBookingServiceController.getEventBookingServicesByEventIdAndUserId(3L, 7L).size());
    }

    @Test
    void deleteEventBookingServiceDelegatesToUseCase() {
        eventBookingServiceController.deleteEventBookingService(10L);

        verify(eventBookingServiceUseCase).deleteEventBookingService(10L);
    }

    private EventBookingService eventBookingServiceWithId(Long id, Long eventBookingId, Long userId,
                                                          Long eventId, Long eventServiceId) {
        User user = new User();
        user.setId(userId);

        Event event = new Event();
        event.setId(eventId);

        EventBooking eventBooking = new EventBooking();
        eventBooking.setId(eventBookingId);
        eventBooking.setUser(user);
        eventBooking.setEvent(event);

        EventService eventService = new EventService();
        eventService.setId(eventServiceId);

        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setId(id);
        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(eventService);
        eventBookingService.setPriceAtPurchase(new BigDecimal("10.00"));
        return eventBookingService;
    }
}
