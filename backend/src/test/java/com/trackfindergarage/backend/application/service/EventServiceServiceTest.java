package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceServiceTest {

    @Mock
    private EventServicePersistencePort eventServicePersistencePort;

    @Mock
    private EventPersistencePort eventPersistencePort;

    @Mock
    private TrackServicePersistencePort trackServicePersistencePort;

    @Mock
    private OrganizerServicePersistencePort organizerServicePersistencePort;

    @InjectMocks
    private EventServiceService eventServiceService;

    @Test
    void createEventServicePersistsWhenTrackServiceMatchesEventTrack() {
        Event event = eventWithId(1L, 10L, 20L);
        com.trackfindergarage.backend.domain.model.TrackService trackService = trackServiceWithId(2L, 20L);
        EventService eventService = eventServiceWithTrackService(1L, 2L);
        eventService.setPrice(new BigDecimal("10.00"));

        when(eventPersistencePort.findById(1L)).thenReturn(Optional.of(event));
        when(trackServicePersistencePort.findById(2L)).thenReturn(Optional.of(trackService));
        when(eventServicePersistencePort.findByEventIdAndTrackServiceId(1L, 2L)).thenReturn(Optional.empty());
        when(eventServicePersistencePort.save(eventService)).thenReturn(eventService);

        EventService created = eventServiceService.createEventService(eventService);

        assertSame(eventService, created);
        assertSame(event, eventService.getEvent());
        assertSame(trackService, eventService.getTrackService());
    }

    @Test
    void createEventServiceThrowsWhenNeitherAssociationIsProvided() {
        EventService eventService = new EventService();
        Event event = new Event();
        event.setId(1L);
        eventService.setEvent(event);
        eventService.setPrice(new BigDecimal("10.00"));

        assertThrows(IllegalArgumentException.class, () -> eventServiceService.createEventService(eventService));
    }

    @Test
    void createEventServiceThrowsWhenTrackServiceDoesNotBelongToEventTrack() {
        Event event = eventWithId(1L, 10L, 20L);
        com.trackfindergarage.backend.domain.model.TrackService trackService = trackServiceWithId(2L, 99L);
        EventService eventService = eventServiceWithTrackService(1L, 2L);
        eventService.setPrice(new BigDecimal("10.00"));

        when(eventPersistencePort.findById(1L)).thenReturn(Optional.of(event));
        when(trackServicePersistencePort.findById(2L)).thenReturn(Optional.of(trackService));

        assertThrows(IllegalArgumentException.class, () -> eventServiceService.createEventService(eventService));
    }

    @Test
    void createEventServiceThrowsWhenTrackServiceAlreadyExistsForEvent() {
        Event event = eventWithId(1L, 10L, 20L);
        com.trackfindergarage.backend.domain.model.TrackService trackService = trackServiceWithId(2L, 20L);
        EventService eventService = eventServiceWithTrackService(1L, 2L);
        eventService.setPrice(new BigDecimal("10.00"));

        when(eventPersistencePort.findById(1L)).thenReturn(Optional.of(event));
        when(trackServicePersistencePort.findById(2L)).thenReturn(Optional.of(trackService));
        when(eventServicePersistencePort.findByEventIdAndTrackServiceId(1L, 2L)).thenReturn(Optional.of(new EventService()));

        assertThrows(DuplicateResourceException.class, () -> eventServiceService.createEventService(eventService));
    }

    @Test
    void getEventServicesByEventIdReturnsPersistenceResult() {
        Event event = eventWithId(1L, 10L, 20L);
        List<EventService> eventServices = List.of(eventServiceWithTrackService(1L, 2L));

        when(eventPersistencePort.findById(1L)).thenReturn(Optional.of(event));
        when(eventServicePersistencePort.findByEventId(1L)).thenReturn(eventServices);

        assertEquals(eventServices, eventServiceService.getEventServicesByEventId(1L));
    }

    @Test
    void getEventServiceByIdThrowsWhenItDoesNotExist() {
        when(eventServicePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventServiceService.getEventServiceById(99L));
    }

    private Event eventWithId(Long id, Long organizerId, Long trackId) {
        Event event = new Event();
        event.setId(id);
        com.trackfindergarage.backend.domain.model.Organizer organizer =
                new com.trackfindergarage.backend.domain.model.Organizer();
        organizer.setIdUser(organizerId);
        com.trackfindergarage.backend.domain.model.Track track = new com.trackfindergarage.backend.domain.model.Track();
        track.setId(trackId);
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(LocalDate.now().plusDays(5));
        return event;
    }

    private com.trackfindergarage.backend.domain.model.TrackService trackServiceWithId(Long id, Long trackId) {
        com.trackfindergarage.backend.domain.model.TrackService trackService =
                new com.trackfindergarage.backend.domain.model.TrackService();
        trackService.setId(id);
        com.trackfindergarage.backend.domain.model.Track track = new com.trackfindergarage.backend.domain.model.Track();
        track.setId(trackId);
        trackService.setTrack(track);
        return trackService;
    }

    private EventService eventServiceWithTrackService(Long eventId, Long trackServiceId) {
        EventService eventService = new EventService();
        Event event = new Event();
        event.setId(eventId);
        eventService.setEvent(event);
        com.trackfindergarage.backend.domain.model.TrackService trackService =
                new com.trackfindergarage.backend.domain.model.TrackService();
        trackService.setId(trackServiceId);
        eventService.setTrackService(trackService);
        return eventService;
    }
}
