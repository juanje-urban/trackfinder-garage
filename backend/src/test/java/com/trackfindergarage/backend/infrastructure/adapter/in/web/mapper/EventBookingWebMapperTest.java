package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventBookingRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBookingWebMapperTest {

    private final EventBookingWebMapper eventBookingWebMapper = new EventBookingWebMapper();

    @Test
    void toDomainMapsCreateRequestToEventBooking() {
        CreateEventBookingRequest request = new CreateEventBookingRequest();
        request.setUserId(1L);
        request.setEventId(2L);

        EventBooking eventBooking = eventBookingWebMapper.toDomain(request);

        assertEquals(1L, eventBooking.getUser().getId());
        assertEquals(2L, eventBooking.getEvent().getId());
    }

    @Test
    void toResponseMapsEventBookingToResponse() {
        User user = new User();
        user.setId(1L);
        user.setDisplayName("user");

        Organizer organizer = new Organizer();
        organizer.setIdUser(3L);
        organizer.setLegalName("Organizer");

        Track track = new Track();
        track.setId(4L);
        track.setName("Track");

        Event event = new Event();
        event.setId(2L);
        event.setEventDate(LocalDate.of(2026, 4, 10));
        event.setOrganizer(organizer);
        event.setTrack(track);

        EventBooking eventBooking = new EventBooking();
        eventBooking.setId(10L);
        eventBooking.setUser(user);
        eventBooking.setEvent(event);
        eventBooking.setBookedAt(LocalDateTime.of(2026, 3, 23, 10, 0));
        eventBooking.setBasePriceAtPurchase(new BigDecimal("30.00"));

        EventBookingResponse response = eventBookingWebMapper.toResponse(eventBooking);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(2L, response.getEventId());
        assertEquals("Organizer", response.getOrganizerLegalName());
        assertEquals("Track", response.getTrackName());
    }
}
