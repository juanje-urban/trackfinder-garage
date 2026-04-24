package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventWebMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventControllerTest {

    private final EventUseCase eventUseCase = mock(EventUseCase.class);
    private final EventWebMapper eventWebMapper = new EventWebMapper();
    private final EventController eventController = new EventController(eventUseCase, eventWebMapper);

    @Test
    void queryEndpointsMapUseCaseResult() {
        Event event = eventWithId(10L, 1L, 2L);

        when(eventUseCase.getFutureEvents()).thenReturn(List.of(event));
        when(eventUseCase.getEventById(10L)).thenReturn(event);
        when(eventUseCase.getRemainingCapacity(10L)).thenReturn(14);

        assertEquals(1, eventController.getFutureEvents().size());
        assertEquals(10L, eventController.getEventById(10L).getId());
    }

    private Event eventWithId(Long id, Long organizerId, Long trackId) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(organizerId);
        organizer.setLegalName("Organizer");

        Track track = new Track();
        track.setId(trackId);
        track.setName("Track");
        track.setShortName("track_demo");

        Event event = new Event();
        event.setId(id);
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(LocalDate.now().plusDays(5));
        event.setBasePrice(new BigDecimal("30.00"));
        event.setMaxParticipants(20);
        event.setDescription("Track day demo con descripciÃ³n larga para pruebas.");
        return event;
    }
}
