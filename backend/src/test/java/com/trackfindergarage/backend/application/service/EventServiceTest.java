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
    void updateEventPersistsWhenExistingEventIsFuture() {
        Event existing = eventWithIds(1L, 2L, LocalDate.now().plusDays(5), new BigDecimal("30.00"));
        existing.setId(5L);
        Event updateRequest = eventWithIds(3L, 4L, LocalDate.now().plusDays(10), new BigDecimal("35.00"));
        Organizer organizer = organizerWithId(3L);
        Track track = trackWithId(4L);

        when(eventPersistencePort.findById(5L)).thenReturn(Optional.of(existing));
        when(organizerPersistencePort.findById(3L)).thenReturn(Optional.of(organizer));
        when(trackPersistencePort.findById(4L)).thenReturn(Optional.of(track));
        when(eventPersistencePort.findByTrackIdAndEventDate(4L, updateRequest.getEventDate())).thenReturn(Optional.empty());
        when(eventPersistencePort.save(existing)).thenReturn(existing);

        Event updated = eventService.updateEvent(5L, updateRequest);

        assertSame(existing, updated);
        assertSame(organizer, existing.getOrganizer());
        assertSame(track, existing.getTrack());
        assertEquals(updateRequest.getEventDate(), existing.getEventDate());
        assertEquals(updateRequest.getBasePrice(), existing.getBasePrice());
        verify(eventPersistencePort).save(existing);
    }

    @Test
    void updateEventAllowsSameTrackAndDateForCurrentEvent() {
        Event existing = eventWithIds(1L, 2L, LocalDate.now().plusDays(5), new BigDecimal("30.00"));
        existing.setId(5L);
        Event updateRequest = eventWithIds(1L, 2L, existing.getEventDate(), new BigDecimal("35.00"));
        Event sameEventFound = eventWithIds(1L, 2L, existing.getEventDate(), new BigDecimal("30.00"));
        sameEventFound.setId(5L);

        when(eventPersistencePort.findById(5L)).thenReturn(Optional.of(existing));
        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.of(organizerWithId(1L)));
        when(trackPersistencePort.findById(2L)).thenReturn(Optional.of(trackWithId(2L)));
        when(eventPersistencePort.findByTrackIdAndEventDate(2L, existing.getEventDate())).thenReturn(Optional.of(sameEventFound));
        when(eventPersistencePort.save(existing)).thenReturn(existing);

        Event updated = eventService.updateEvent(5L, updateRequest);

        assertSame(existing, updated);
    }

    @Test
    void createEventThrowsWhenOrganizerDoesNotExist() {
        Event event = eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00"));

        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(event));
    }

    @Test
    void createEventThrowsWhenTrackDoesNotExist() {
        Event event = eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00"));

        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.of(organizerWithId(1L)));
        when(trackPersistencePort.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(event));
    }

    @Test
    void createEventThrowsWhenOrganizerIdIsMissing() {
        Event event = new Event();
        event.setTrack(trackWithId(2L));
        event.setEventDate(LocalDate.now().plusDays(10));
        event.setBasePrice(new BigDecimal("30.00"));

        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));
    }

    @Test
    void createEventThrowsWhenTrackIdIsMissing() {
        Event event = new Event();
        event.setOrganizer(organizerWithId(1L));
        event.setEventDate(LocalDate.now().plusDays(10));
        event.setBasePrice(new BigDecimal("30.00"));

        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));
    }

    @Test
    void createEventThrowsWhenEventDateIsMissing() {
        Event event = new Event();
        event.setOrganizer(organizerWithId(1L));
        event.setTrack(trackWithId(2L));
        event.setBasePrice(new BigDecimal("30.00"));

        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));
    }

    @Test
    void createEventThrowsWhenEventDateIsNotFuture() {
        Event event = eventWithIds(1L, 2L, LocalDate.now(), new BigDecimal("30.00"));

        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));
    }

    @Test
    void createEventThrowsWhenBasePriceIsMissing() {
        Event event = eventWithIds(1L, 2L, LocalDate.now().plusDays(10), null);

        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));
    }

    @Test
    void createEventThrowsWhenBasePriceIsNotPositive() {
        Event event = eventWithIds(1L, 2L, LocalDate.now().plusDays(10), BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(event));
    }

    @Test
    void deleteEventDelegatesToPersistenceAfterLoadingExistingEvent() {
        Event event = eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00"));
        event.setId(8L);

        when(eventPersistencePort.findById(8L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(8L);

        verify(eventPersistencePort).delete(event);
    }

    @Test
    void getAllEventsReturnsPersistenceResult() {
        List<Event> events = List.of(eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00")));

        when(eventPersistencePort.findAll()).thenReturn(events);

        assertEquals(events, eventService.getAllEvents());
    }

    @Test
    void getEventsByOrganizerIdReturnsPersistenceResultWhenOrganizerExists() {
        List<Event> events = List.of(eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00")));

        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.of(organizerWithId(1L)));
        when(eventPersistencePort.findByOrganizerIdUser(1L)).thenReturn(events);

        assertEquals(events, eventService.getEventsByOrganizerId(1L));
    }

    @Test
    void getEventsByOrganizerIdThrowsWhenOrganizerDoesNotExist() {
        when(organizerPersistencePort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventsByOrganizerId(1L));
    }

    @Test
    void getEventsByTrackIdReturnsPersistenceResultWhenTrackExists() {
        List<Event> events = List.of(eventWithIds(1L, 2L, LocalDate.now().plusDays(10), new BigDecimal("30.00")));

        when(trackPersistencePort.findById(2L)).thenReturn(Optional.of(trackWithId(2L)));
        when(eventPersistencePort.findByTrackId(2L)).thenReturn(events);

        assertEquals(events, eventService.getEventsByTrackId(2L));
    }

    @Test
    void getEventsByTrackIdThrowsWhenTrackDoesNotExist() {
        when(trackPersistencePort.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventsByTrackId(2L));
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
    void getEventsByDateRangeThrowsWhenStartDateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getEventsByDateRange(null, LocalDate.now().plusDays(1)));
    }

    @Test
    void getEventsByDateRangeThrowsWhenEndDateIsNull() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getEventsByDateRange(LocalDate.now(), null));
    }

    @Test
    void getEventsByDateRangeThrowsWhenStartDateIsAfterEndDate() {
        assertThrows(IllegalArgumentException.class,
                () -> eventService.getEventsByDateRange(LocalDate.now().plusDays(3), LocalDate.now().plusDays(1)));
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
