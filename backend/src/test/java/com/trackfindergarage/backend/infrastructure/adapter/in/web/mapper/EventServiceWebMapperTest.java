package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventServiceRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EventServiceWebMapperTest {

    private final EventServiceWebMapper eventServiceWebMapper = new EventServiceWebMapper();

    @Test
    void toDomainMapsCreateRequestToEventService() {
        CreateEventServiceRequest request = new CreateEventServiceRequest();
        request.setEventId(1L);
        request.setTrackServiceId(2L);
        request.setPrice(new BigDecimal("10.00"));

        EventService eventService = eventServiceWebMapper.toDomain(request);

        assertEquals(1L, eventService.getEvent().getId());
        assertEquals(2L, eventService.getTrackService().getId());
        assertNull(eventService.getOrganizerService());
        assertEquals(new BigDecimal("10.00"), eventService.getPrice());
    }

    @Test
    void updateDomainMapsUpdateRequestToEventService() {
        EventService eventService = new EventService();
        UpdateEventServiceRequest request = new UpdateEventServiceRequest();
        request.setEventId(3L);
        request.setOrganizerServiceId(4L);
        request.setPrice(new BigDecimal("15.00"));

        eventServiceWebMapper.updateDomain(eventService, request);

        assertEquals(3L, eventService.getEvent().getId());
        assertEquals(4L, eventService.getOrganizerService().getId());
        assertNull(eventService.getTrackService());
        assertEquals(new BigDecimal("15.00"), eventService.getPrice());
    }

    @Test
    void toResponseMapsEventServiceToResponse() {
        Event event = new Event();
        event.setId(1L);

        com.trackfindergarage.backend.domain.model.TrackService trackService =
                new com.trackfindergarage.backend.domain.model.TrackService();
        trackService.setId(2L);
        com.trackfindergarage.backend.domain.model.Service service =
                new com.trackfindergarage.backend.domain.model.Service();
        service.setName("Parking");
        trackService.setService(service);

        EventService eventService = new EventService();
        eventService.setId(10L);
        eventService.setEvent(event);
        eventService.setTrackService(trackService);
        eventService.setPrice(new BigDecimal("12.00"));

        EventServiceResponse response = eventServiceWebMapper.toResponse(eventService);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getEventId());
        assertEquals(2L, response.getTrackServiceId());
        assertEquals("Parking", response.getTrackServiceName());
        assertEquals(new BigDecimal("12.00"), response.getPrice());
    }
}
