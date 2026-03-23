package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
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
class EventBookingServiceServiceTest {

    @Mock
    private EventBookingServicePersistencePort eventBookingServicePersistencePort;

    @Mock
    private EventBookingPersistencePort eventBookingPersistencePort;

    @Mock
    private EventServicePersistencePort eventServicePersistencePort;

    @Mock
    private EventPersistencePort eventPersistencePort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @InjectMocks
    private EventBookingServiceService eventBookingServiceService;

    @Test
    void createEventBookingServicePersistsWhenDataIsValid() {
        EventBooking eventBooking = eventBookingWithId(1L, 7L, 2L, LocalDate.now().plusDays(20));
        EventService eventService = eventServiceWithId(3L, 2L);
        EventBookingService eventBookingService = eventBookingServiceWithIds(1L, 3L);

        when(eventBookingPersistencePort.findById(1L)).thenReturn(Optional.of(eventBooking));
        when(eventServicePersistencePort.findById(3L)).thenReturn(Optional.of(eventService));
        when(eventBookingServicePersistencePort.findByEventBookingIdAndEventServiceId(1L, 3L)).thenReturn(Optional.empty());
        when(eventBookingServicePersistencePort.save(eventBookingService)).thenReturn(eventBookingService);

        EventBookingService created = eventBookingServiceService.createEventBookingService(eventBookingService);

        assertSame(eventBookingService, created);
        assertSame(eventBooking, eventBookingService.getEventBooking());
        assertSame(eventService, eventBookingService.getEventService());
        assertEquals(eventService.getPrice(), eventBookingService.getPriceAtPurchase());
    }

    @Test
    void createEventBookingServiceThrowsWhenDuplicateExists() {
        EventBooking eventBooking = eventBookingWithId(1L, 7L, 2L, LocalDate.now().plusDays(20));
        EventService eventService = eventServiceWithId(3L, 2L);
        EventBookingService eventBookingService = eventBookingServiceWithIds(1L, 3L);

        when(eventBookingPersistencePort.findById(1L)).thenReturn(Optional.of(eventBooking));
        when(eventServicePersistencePort.findById(3L)).thenReturn(Optional.of(eventService));
        when(eventBookingServicePersistencePort.findByEventBookingIdAndEventServiceId(1L, 3L))
                .thenReturn(Optional.of(new EventBookingService()));

        assertThrows(DuplicateResourceException.class,
                () -> eventBookingServiceService.createEventBookingService(eventBookingService));
    }

    @Test
    void createEventBookingServiceThrowsWhenEventServiceBelongsToAnotherEvent() {
        EventBooking eventBooking = eventBookingWithId(1L, 7L, 2L, LocalDate.now().plusDays(20));
        EventService eventService = eventServiceWithId(3L, 99L);
        EventBookingService eventBookingService = eventBookingServiceWithIds(1L, 3L);

        when(eventBookingPersistencePort.findById(1L)).thenReturn(Optional.of(eventBooking));
        when(eventServicePersistencePort.findById(3L)).thenReturn(Optional.of(eventService));

        assertThrows(IllegalArgumentException.class,
                () -> eventBookingServiceService.createEventBookingService(eventBookingService));
    }

    @Test
    void createEventBookingServiceThrowsWhenBookingEventIsNotFuture() {
        EventBooking eventBooking = eventBookingWithId(1L, 7L, 2L, LocalDate.now());
        EventService eventService = eventServiceWithId(3L, 2L);
        EventBookingService eventBookingService = eventBookingServiceWithIds(1L, 3L);

        when(eventBookingPersistencePort.findById(1L)).thenReturn(Optional.of(eventBooking));
        when(eventServicePersistencePort.findById(3L)).thenReturn(Optional.of(eventService));

        assertThrows(IllegalArgumentException.class,
                () -> eventBookingServiceService.createEventBookingService(eventBookingService));
    }

    @Test
    void deleteEventBookingServiceDelegatesToPersistence() {
        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setId(8L);

        when(eventBookingServicePersistencePort.findById(8L)).thenReturn(Optional.of(eventBookingService));

        eventBookingServiceService.deleteEventBookingService(8L);

        verify(eventBookingServicePersistencePort).delete(eventBookingService);
    }

    @Test
    void getEventBookingServicesByEventIdAndUserIdReturnsPersistenceResult() {
        List<EventBookingService> eventBookingServices = List.of(eventBookingServiceWithIds(1L, 3L));

        when(eventPersistencePort.findById(2L)).thenReturn(Optional.of(eventWithId(2L, LocalDate.now().plusDays(20))));
        when(userPersistencePort.findById(7L)).thenReturn(Optional.of(userWithId(7L)));
        when(eventBookingServicePersistencePort.findByEventBookingEventIdAndEventBookingUserId(2L, 7L))
                .thenReturn(eventBookingServices);

        assertEquals(eventBookingServices, eventBookingServiceService.getEventBookingServicesByEventIdAndUserId(2L, 7L));
    }

    @Test
    void getEventBookingServiceByIdThrowsWhenItDoesNotExist() {
        when(eventBookingServicePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventBookingServiceService.getEventBookingServiceById(99L));
    }

    private EventBookingService eventBookingServiceWithIds(Long eventBookingId, Long eventServiceId) {
        EventBookingService eventBookingService = new EventBookingService();
        EventBooking eventBooking = new EventBooking();
        eventBooking.setId(eventBookingId);
        eventBookingService.setEventBooking(eventBooking);
        EventService eventService = new EventService();
        eventService.setId(eventServiceId);
        eventBookingService.setEventService(eventService);
        return eventBookingService;
    }

    private EventBooking eventBookingWithId(Long id, Long userId, Long eventId, LocalDate eventDate) {
        EventBooking eventBooking = new EventBooking();
        eventBooking.setId(id);
        eventBooking.setUser(userWithId(userId));
        eventBooking.setEvent(eventWithId(eventId, eventDate));
        return eventBooking;
    }

    private EventService eventServiceWithId(Long id, Long eventId) {
        EventService eventService = new EventService();
        eventService.setId(id);
        eventService.setEvent(eventWithId(eventId, LocalDate.now().plusDays(20)));
        eventService.setPrice(new BigDecimal("10.00"));
        return eventService;
    }

    private Event eventWithId(Long id, LocalDate eventDate) {
        Event event = new Event();
        event.setId(id);
        event.setEventDate(eventDate);
        event.setBasePrice(new BigDecimal("30.00"));

        Organizer organizer = new Organizer();
        organizer.setIdUser(5L);
        organizer.setLegalName("Organizer");
        event.setOrganizer(organizer);

        Track track = new Track();
        track.setId(6L);
        track.setName("Track");
        event.setTrack(track);
        return event;
    }

    private User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        user.setDisplayName("user");
        return user;
    }
}
