package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventServiceUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventServiceWebMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServiceControllerTest {

    private final EventServiceUseCase eventServiceUseCase = mock(EventServiceUseCase.class);
    private final EventServiceWebMapper eventServiceWebMapper = new EventServiceWebMapper();
    private final EventServiceController eventServiceController =
            new EventServiceController(eventServiceUseCase, eventServiceWebMapper);

    @Test
    void createAndUpdateEventServiceDelegateToUseCaseAndReturnMappedResponse() {
        CreateEventServiceRequest createRequest = new CreateEventServiceRequest();
        createRequest.setEventId(1L);
        createRequest.setTrackServiceId(2L);
        createRequest.setPrice(new BigDecimal("10.00"));

        UpdateEventServiceRequest updateRequest = new UpdateEventServiceRequest();
        updateRequest.setEventId(1L);
        updateRequest.setOrganizerServiceId(3L);
        updateRequest.setPrice(new BigDecimal("12.00"));

        EventService eventService = eventServiceWithId(10L, 1L);

        when(eventServiceUseCase.createEventService(any(EventService.class))).thenReturn(eventService);
        when(eventServiceUseCase.updateEventService(eq(10L), any(EventService.class))).thenReturn(eventService);

        EventServiceResponse created = eventServiceController.createEventService(createRequest);
        EventServiceResponse updated = eventServiceController.updateEventService(10L, updateRequest);

        assertEquals(10L, created.getId());
        assertEquals(10L, updated.getId());
    }

    @Test
    void queryEndpointsMapUseCaseResult() {
        EventService eventService = eventServiceWithId(10L, 1L);

        when(eventServiceUseCase.getAllEventServices()).thenReturn(List.of(eventService));
        when(eventServiceUseCase.getEventServiceById(10L)).thenReturn(eventService);
        when(eventServiceUseCase.getEventServicesByEventId(1L)).thenReturn(List.of(eventService));

        assertEquals(1, eventServiceController.getAllEventServices().size());
        assertEquals(10L, eventServiceController.getEventServiceById(10L).getId());
        assertEquals(1, eventServiceController.getEventServicesByEventId(1L).size());
    }

    @Test
    void deleteEventServiceDelegatesToUseCase() {
        eventServiceController.deleteEventService(10L);

        verify(eventServiceUseCase).deleteEventService(10L);
    }

    private EventService eventServiceWithId(Long id, Long eventId) {
        Event event = new Event();
        event.setId(eventId);

        EventService eventService = new EventService();
        eventService.setId(id);
        eventService.setEvent(event);
        eventService.setPrice(new BigDecimal("10.00"));
        return eventService;
    }
}
