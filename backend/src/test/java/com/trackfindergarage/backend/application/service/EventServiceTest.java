package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventPersistencePort eventPersistencePort;

    @Mock
    private OrganizerPersistencePort organizerPersistencePort;

    @Mock
    private TrackPersistencePort trackPersistencePort;

    @InjectMocks
    private EventService eventService;

    @Test
    void createEventPersistsWhenDataIsValid() {
        Event event = eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00"));
        Organizer organizer = organizerWithId(1L);
        Track track = trackWithId(2L);

        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.of(organizer));
        when(trackPersistencePort.findById(2L)).thenReturn(Optional.of(track));
        when(eventPersistencePort.findByTrackIdAndEventDate(2L, event.getEventDate())).thenReturn(Optional.empty());
        when(eventPersistencePort.save(event)).thenReturn(event);

        Event created = eventService.createEvent(event);

        assertSame(event, created);
        assertSame(organizer, event.getOrganizer());
        assertSame(track, event.getTrack());
        verify(eventPersistencePort).save(event);
    }

    @Test
    void createEventThrowsWhenSameTrackAndDateAlreadyExists() {
        Event event = eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00"));
        Event existing = eventWithIds(9L, 2L, event.getEventDate(), new BigDecimal("20.00"));
        existing.setId(8L);

        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.of(organizerWithId(1L)));
        when(trackPersistencePort.findById(2L)).thenReturn(Optional.of(trackWithId(2L)));
        when(eventPersistencePort.findByTrackIdAndEventDate(2L, event.getEventDate())).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> eventService.createEvent(event));
    }

    @Test
    void updateEventThrowsWhenExistingEventIsPast() {
        Event existing = eventWithIds(1L, 2L, LocalDate.now().minusDays(1), new BigDecimal("30.00"));
        existing.setId(5L);
        Event updateRequest = eventWithIds(1L, 2L, LocalDate.now().plusDays(2), new BigDecimal("35.00"));

        when(eventPersistencePort.findById(5L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> eventService.updateEvent(5L, updateRequest));
    }

    @Test
    void getEventsByDateRangeReturnsPersistenceResult() {
        List<Event> events = List.of(eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00")));
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(30);

        when(eventPersistencePort.findByEventDateBetween(start, end)).thenReturn(events);

        assertEquals(events, eventService.getEventsByDateRange(start, end));
    }

    @Test
    void getEventByIdThrowsWhenEventDoesNotExist() {
        when(eventPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(99L));
    }

    private Event eventWithIds(Long organizerId, Long trackId, LocalDate date, BigDecimal basePrice) {
        Event event = new Event();
        event.setOrganizer(organizerWithId(organizerId));
        event.setTrack(trackWithId(trackId));
        event.setEventDate(date);
        event.setBasePrice(basePrice);
        return event;
    }

    private Organizer organizerWithId(Long id) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(id);
        organizer.setLegalName("Organizer");
        return organizer;
    }

    private Track trackWithId(Long id) {
        Track track = new Track();
        track.setId(id);
        track.setName("Track");
        return track;
    }
}
