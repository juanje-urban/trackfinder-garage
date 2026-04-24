package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventWebMapperTest {

    private final EventWebMapper eventWebMapper = new EventWebMapper();

    @Test
    void toResponseMapsEventToResponse() {
        Organizer organizer = new Organizer();
        organizer.setIdUser(1L);
        organizer.setLegalName("Organizer SL");
        Track track = new Track();
        track.setId(2L);
        track.setName("Jarama");
        track.setShortName("jarama");

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
        assertEquals("jarama", response.getTrackShortName());
        assertEquals(30, response.getMaxParticipants());
        assertEquals(12, response.getRemainingCapacity());
        assertEquals("Jornada premium con acceso a paddock y cronometraje opcional.", response.getDescription());
    }
}
