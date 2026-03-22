package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventWebMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventControllerTest {

    private final EventUseCase eventUseCase = mock(EventUseCase.class);
    private final EventWebMapper eventWebMapper = new EventWebMapper();
    private final EventController eventController = new EventController(eventUseCase, eventWebMapper);

    @Test
    void createAndUpdateEventDelegateToUseCaseAndReturnMappedResponse() {
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setOrganizerId(1L);
        createRequest.setTrackId(2L);
        createRequest.setEventDate(LocalDate.now().plusDays(10));
        createRequest.setBasePrice(new BigDecimal("30.00"));

        UpdateEventRequest updateRequest = new UpdateEventRequest();
        updateRequest.setOrganizerId(1L);
        updateRequest.setTrackId(2L);
        updateRequest.setEventDate(LocalDate.now().plusDays(20));
        updateRequest.setBasePrice(new BigDecimal("35.00"));

        Event event = eventWithId(10L, 1L, 2L);

        when(eventUseCase.createEvent(any(Event.class))).thenReturn(event);
        when(eventUseCase.updateEvent(eq(10L), any(Event.class))).thenReturn(event);

        EventResponse created = eventController.createEvent(createRequest);
        EventResponse updated = eventController.updateEvent(10L, updateRequest);

        assertEquals(10L, created.getId());
        assertEquals(10L, updated.getId());
    }

    @Test
    void queryEndpointsMapUseCaseResult() {
        Event event = eventWithId(10L, 1L, 2L);
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(30);

        when(eventUseCase.getAllEvents()).thenReturn(List.of(event));
        when(eventUseCase.getEventById(10L)).thenReturn(event);
        when(eventUseCase.getEventsByOrganizerId(1L)).thenReturn(List.of(event));
        when(eventUseCase.getEventsByTrackId(2L)).thenReturn(List.of(event));
        when(eventUseCase.getEventsByDateRange(start, end)).thenReturn(List.of(event));

        assertEquals(1, eventController.getAllEvents().size());
        assertEquals(10L, eventController.getEventById(10L).getId());
        assertEquals(1, eventController.getEventsByOrganizerId(1L).size());
        assertEquals(1, eventController.getEventsByTrackId(2L).size());
        assertEquals(1, eventController.getEventsByDateRange(start, end).size());
    }

    @Test
    void deleteEventDelegatesToUseCase() {
        eventController.deleteEvent(10L);

        verify(eventUseCase).deleteEvent(10L);
    }

    private Event eventWithId(Long id, Long organizerId, Long trackId) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(organizerId);
        organizer.setLegalName("Organizer");

        Track track = new Track();
        track.setId(trackId);
        track.setName("Track");

        Event event = new Event();
        event.setId(id);
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(LocalDate.now().plusDays(5));
        event.setBasePrice(new BigDecimal("30.00"));
        return event;
    }
}
