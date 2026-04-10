package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventWebMapperTest {

    private final EventWebMapper eventWebMapper = new EventWebMapper();

    @Test
    void toDomainMapsCreateRequestToEvent() {
        CreateEventRequest request = new CreateEventRequest();
        request.setOrganizerId(1L);
        request.setTrackId(2L);
        request.setEventDate(LocalDate.of(2026, 4, 1));
        request.setBasePrice(new BigDecimal("30.00"));
        request.setMaxParticipants(20);
        request.setDescription(" Jornada completa con tandas libres y briefing inicial. ");

        Event event = eventWebMapper.toDomain(request);

        assertEquals(1L, event.getOrganizer().getIdUser());
        assertEquals(2L, event.getTrack().getId());
        assertEquals(LocalDate.of(2026, 4, 1), event.getEventDate());
        assertEquals(new BigDecimal("30.00"), event.getBasePrice());
        assertEquals(20, event.getMaxParticipants());
        assertEquals("Jornada completa con tandas libres y briefing inicial.", event.getDescription());
    }

    @Test
    void updateDomainMapsUpdateRequestToEvent() {
        Event event = new Event();
        UpdateEventRequest request = new UpdateEventRequest();
        request.setOrganizerId(3L);
        request.setTrackId(4L);
        request.setEventDate(LocalDate.of(2026, 5, 1));
        request.setBasePrice(new BigDecimal("40.00"));
        request.setMaxParticipants(25);
        request.setDescription(" Sesion abierta con grupos por ritmo. ");

        eventWebMapper.updateDomain(event, request);

        assertEquals(3L, event.getOrganizer().getIdUser());
        assertEquals(4L, event.getTrack().getId());
        assertEquals(LocalDate.of(2026, 5, 1), event.getEventDate());
        assertEquals(new BigDecimal("40.00"), event.getBasePrice());
        assertEquals(25, event.getMaxParticipants());
        assertEquals("Sesion abierta con grupos por ritmo.", event.getDescription());
    }

    @Test
    void toResponseMapsEventToResponse() {
        Organizer organizer = new Organizer();
        organizer.setIdUser(1L);
        organizer.setLegalName("Organizer SL");
        Track track = new Track();
        track.setId(2L);
        track.setName("Jarama");

        Event event = new Event();
        event.setId(10L);
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(LocalDate.of(2026, 6, 1));
        event.setBasePrice(new BigDecimal("50.00"));
        event.setMaxParticipants(30);
        event.setDescription("Jornada premium con acceso a paddock y cronometraje opcional.");

        EventResponse response = eventWebMapper.toResponse(event, 12);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getOrganizerId());
        assertEquals("Organizer SL", response.getOrganizerLegalName());
        assertEquals(2L, response.getTrackId());
        assertEquals("Jarama", response.getTrackName());
        assertEquals(30, response.getMaxParticipants());
        assertEquals(12, response.getRemainingCapacity());
        assertEquals("Jornada premium con acceso a paddock y cronometraje opcional.", response.getDescription());
    }
}
