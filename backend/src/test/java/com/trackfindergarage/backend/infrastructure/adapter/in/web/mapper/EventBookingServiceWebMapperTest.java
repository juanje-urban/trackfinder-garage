package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventBookingServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingServiceResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBookingServiceWebMapperTest {

    private final EventBookingServiceWebMapper eventBookingServiceWebMapper = new EventBookingServiceWebMapper();

    @Test
    void toDomainMapsCreateRequestToEventBookingService() {
        CreateEventBookingServiceRequest request = new CreateEventBookingServiceRequest();
        request.setEventBookingId(1L);
        request.setEventServiceId(2L);

        EventBookingService eventBookingService = eventBookingServiceWebMapper.toDomain(request);

        assertEquals(1L, eventBookingService.getEventBooking().getId());
        assertEquals(2L, eventBookingService.getEventService().getId());
    }

    @Test
    void toResponseMapsEventBookingServiceToResponse() {
        User user = new User();
        user.setId(7L);

        Event event = new Event();
        event.setId(3L);

        EventBooking eventBooking = new EventBooking();
        eventBooking.setId(1L);
        eventBooking.setUser(user);
        eventBooking.setEvent(event);

        EventService eventService = new EventService();
        eventService.setId(2L);

        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setId(10L);
        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(eventService);
        eventBookingService.setPriceAtPurchase(new BigDecimal("10.00"));

        EventBookingServiceResponse response = eventBookingServiceWebMapper.toResponse(eventBookingService);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getEventBookingId());
        assertEquals(7L, response.getUserId());
        assertEquals(3L, response.getEventId());
        assertEquals(2L, response.getEventServiceId());
    }
}
