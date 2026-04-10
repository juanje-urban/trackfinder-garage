package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ConflictException;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventBookingServiceTest {

    @Mock
    private EventBookingPersistencePort eventBookingPersistencePort;

    @Mock
    private EventBookingServicePersistencePort eventBookingServicePersistencePort;

    @Mock
    private EventServicePersistencePort eventServicePersistencePort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private EventPersistencePort eventPersistencePort;

    @InjectMocks
    private EventBookingService eventBookingService;

    @Test
    void createEventBookingPersistsWhenDataIsValid() {
        EventBooking eventBooking = eventBookingWithIds(1L, 2L);
        User user = userWithId(1L);
        Event event = eventWithId(2L, LocalDate.now().plusDays(20));

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(eventPersistencePort.findById(2L)).thenReturn(Optional.of(event));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 2L)).thenReturn(Optional.empty());
        when(eventBookingPersistencePort.countByEventId(2L)).thenReturn(1L);
        when(eventBookingPersistencePort.save(eventBooking)).thenReturn(eventBooking);

        EventBooking created = eventBookingService.createEventBooking(eventBooking);

        assertSame(eventBooking, created);
        assertSame(user, eventBooking.getUser());
        assertSame(event, eventBooking.getEvent());
        assertEquals(event.getBasePrice(), eventBooking.getBasePriceAtPurchase());
        verify(eventBookingPersistencePort).save(eventBooking);
    }

    @Test
    void createEventBookingThrowsWhenDuplicateExists() {
        EventBooking eventBooking = eventBookingWithIds(1L, 2L);

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(eventPersistencePort.findById(2L)).thenReturn(Optional.of(eventWithId(2L, LocalDate.now().plusDays(20))));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 2L)).thenReturn(Optional.of(new EventBooking()));

        assertThrows(DuplicateResourceException.class, () -> eventBookingService.createEventBooking(eventBooking));
    }

    @Test
    void createEventBookingThrowsWhenEventIsNotFuture() {
        EventBooking eventBooking = eventBookingWithIds(1L, 2L);

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(eventPersistencePort.findById(2L)).thenReturn(Optional.of(eventWithId(2L, LocalDate.now())));

        assertThrows(IllegalArgumentException.class, () -> eventBookingService.createEventBooking(eventBooking));
    }

    @Test
    void createEventBookingThrowsWhenEventIsFull() {
        EventBooking eventBooking = eventBookingWithIds(1L, 2L);

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(eventPersistencePort.findById(2L)).thenReturn(Optional.of(eventWithId(2L, LocalDate.now().plusDays(20))));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 2L)).thenReturn(Optional.empty());
        when(eventBookingPersistencePort.countByEventId(2L)).thenReturn(20L);

        assertThrows(ConflictException.class, () -> eventBookingService.createEventBooking(eventBooking));
    }

    @Test
    void checkoutEventBookingCreatesBookingAndSelectedServicesForAuthenticatedUser() {
        User user = userWithId(1L);
        Event event = eventWithId(2L, LocalDate.now().plusDays(20));
        EventService eventService = eventServiceWithId(9L, event);
        EventBooking persistedBooking = eventBookingWithIds(1L, 2L);
        persistedBooking.setId(15L);
        persistedBooking.setUser(user);
        persistedBooking.setEvent(event);

        when(userPersistencePort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(eventPersistencePort.findById(2L)).thenReturn(Optional.of(event));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 2L)).thenReturn(Optional.empty());
        when(eventBookingPersistencePort.countByEventId(2L)).thenReturn(1L);
        when(eventBookingPersistencePort.save(any(EventBooking.class))).thenReturn(persistedBooking);
        when(eventServicePersistencePort.findById(9L)).thenReturn(Optional.of(eventService));

        EventBooking createdBooking = eventBookingService.checkoutEventBooking("user@example.com", 2L, List.of(9L));

        assertSame(persistedBooking, createdBooking);
        verify(eventBookingPersistencePort).save(any(EventBooking.class));
        verify(eventBookingServicePersistencePort).save(argThat(savedService ->
                savedService.getEventBooking() == persistedBooking
                        && savedService.getEventService() == eventService
                        && eventService.getPrice().compareTo(savedService.getPriceAtPurchase()) == 0
        ));
    }

    @Test
    void checkoutEventBookingThrowsWhenAuthenticatedUserIsNotStandardUser() {
        User organizerUser = userWithId(1L);
        organizerUser.setRole(roleWithName("ORGANIZER"));

        when(userPersistencePort.findByEmail("user@example.com")).thenReturn(Optional.of(organizerUser));

        assertThrows(
                AccessDeniedException.class,
                () -> eventBookingService.checkoutEventBooking("user@example.com", 2L, List.of(9L))
        );
    }

    @Test
    void checkoutEventBookingThrowsWhenSelectedServiceBelongsToAnotherEvent() {
        User user = userWithId(1L);
        Event requestedEvent = eventWithId(2L, LocalDate.now().plusDays(20));
        Event otherEvent = eventWithId(3L, LocalDate.now().plusDays(20));
        EventService eventService = eventServiceWithId(9L, otherEvent);
        EventBooking persistedBooking = eventBookingWithIds(1L, 2L);
        persistedBooking.setId(15L);
        persistedBooking.setUser(user);
        persistedBooking.setEvent(requestedEvent);

        when(userPersistencePort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(eventPersistencePort.findById(2L)).thenReturn(Optional.of(requestedEvent));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 2L)).thenReturn(Optional.empty());
        when(eventBookingPersistencePort.countByEventId(2L)).thenReturn(1L);
        when(eventBookingPersistencePort.save(any(EventBooking.class))).thenReturn(persistedBooking);
        when(eventServicePersistencePort.findById(9L)).thenReturn(Optional.of(eventService));

        assertThrows(
                IllegalArgumentException.class,
                () -> eventBookingService.checkoutEventBooking("user@example.com", 2L, List.of(9L))
        );
    }

    @Test
    void deleteEventBookingDeletesChildrenAndBookingWhenDateIsAtLeast14DaysAway() {
        EventBooking eventBooking = eventBookingWithIds(1L, 2L);
        eventBooking.setId(9L);
        eventBooking.setEvent(eventWithId(2L, LocalDate.now().plusDays(14)));
        eventBooking.getUser().setEmail("user@example.com");
        com.trackfindergarage.backend.domain.model.EventBookingService child =
                new com.trackfindergarage.backend.domain.model.EventBookingService();
        child.setId(3L);

        when(eventBookingPersistencePort.findById(9L)).thenReturn(Optional.of(eventBooking));
        when(eventBookingServicePersistencePort.findByEventBookingId(9L)).thenReturn(List.of(child));

        eventBookingService.deleteEventBooking(9L);

        verify(eventBookingServicePersistencePort).delete(child);
        verify(eventBookingPersistencePort).delete(eventBooking);
    }

    @Test
    void deleteOwnEventBookingThrowsWhenBookingBelongsToAnotherUser() {
        EventBooking eventBooking = eventBookingWithIds(1L, 2L);
        eventBooking.setId(9L);
        eventBooking.getUser().setEmail("other@example.com");

        when(eventBookingPersistencePort.findById(9L)).thenReturn(Optional.of(eventBooking));

        assertThrows(
                AccessDeniedException.class,
                () -> eventBookingService.deleteOwnEventBooking("user@example.com", 9L)
        );
    }

    @Test
    void deleteEventBookingThrowsWhenDateIsLessThan14DaysAway() {
        EventBooking eventBooking = eventBookingWithIds(1L, 2L);
        eventBooking.setId(9L);
        eventBooking.setEvent(eventWithId(2L, LocalDate.now().plusDays(13)));

        when(eventBookingPersistencePort.findById(9L)).thenReturn(Optional.of(eventBooking));

        assertThrows(IllegalArgumentException.class, () -> eventBookingService.deleteEventBooking(9L));
    }

    @Test
    void getEventBookingsByUserIdReturnsPersistenceResult() {
        List<EventBooking> eventBookings = List.of(eventBookingWithIds(1L, 2L));

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(eventBookingPersistencePort.findByUserId(1L)).thenReturn(eventBookings);

        assertEquals(eventBookings, eventBookingService.getEventBookingsByUserId(1L));
    }

    @Test
    void getEventBookingsByEventIdReturnsPersistenceResult() {
        List<EventBooking> eventBookings = List.of(eventBookingWithIds(1L, 2L));

        when(eventPersistencePort.findById(2L)).thenReturn(Optional.of(eventWithId(2L, LocalDate.now().plusDays(20))));
        when(eventBookingPersistencePort.findByEventId(2L)).thenReturn(eventBookings);

        assertEquals(eventBookings, eventBookingService.getEventBookingsByEventId(2L));
    }

    @Test
    void getEventBookingByIdThrowsWhenItDoesNotExist() {
        when(eventBookingPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventBookingService.getEventBookingById(99L));
    }

    private EventBooking eventBookingWithIds(Long userId, Long eventId) {
        EventBooking eventBooking = new EventBooking();
        eventBooking.setUser(userWithId(userId));
        eventBooking.setEvent(eventWithId(eventId, LocalDate.now().plusDays(20)));
        return eventBooking;
    }

    private User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        user.setDisplayName("user");
        user.setEmail("user@example.com");
        user.setRole(roleWithName("USER"));
        return user;
    }

    private Role roleWithName(String roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
        return role;
    }

    private EventService eventServiceWithId(Long id, Event event) {
        EventService eventService = new EventService();
        eventService.setId(id);
        eventService.setEvent(event);
        eventService.setPrice(new BigDecimal("12.00"));
        return eventService;
    }

    private Event eventWithId(Long id, LocalDate eventDate) {
        Event event = new Event();
        event.setId(id);
        event.setEventDate(eventDate);
        event.setBasePrice(new BigDecimal("30.00"));
        event.setMaxParticipants(20);

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
}
