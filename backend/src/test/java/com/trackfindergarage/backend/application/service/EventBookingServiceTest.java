package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ConflictException;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void checkoutEventBookingCreatesBookingAndDistinctBookedServices() {
        User user = user(1L, "driver", "USER");
        Event event = event(5L, LocalDate.now().plusDays(20), 10);
        EventService firstService = eventService(11L, event, "20.00");
        EventService secondService = eventService(12L, event, "30.00");

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(eventPersistencePort.findById(5L)).thenReturn(Optional.of(event));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 5L)).thenReturn(Optional.empty());
        when(eventBookingPersistencePort.countByEventId(5L)).thenReturn(0L);
        when(eventBookingPersistencePort.save(any(EventBooking.class))).thenAnswer(invocation -> {
            EventBooking savedBooking = invocation.getArgument(0);
            savedBooking.setId(50L);
            return savedBooking;
        });
        when(eventServicePersistencePort.findById(11L)).thenReturn(Optional.of(firstService));
        when(eventServicePersistencePort.findById(12L)).thenReturn(Optional.of(secondService));
        when(eventBookingServicePersistencePort.save(any(com.trackfindergarage.backend.domain.model.EventBookingService.class)))
                .then(returnsFirstArg());

        EventBooking createdBooking = eventBookingService.checkoutEventBooking(
                " DRIVER@example.com ",
                5L,
                java.util.Arrays.asList(11L, 11L, null, 12L),
                true
        );

        assertEquals(50L, createdBooking.getId());
        assertSame(user, createdBooking.getUser());
        assertSame(event, createdBooking.getEvent());
        assertEquals(new BigDecimal("120.00"), createdBooking.getBasePriceAtPurchase());
        assertNotNull(createdBooking.getBookedAt());
        assertEquals(true, createdBooking.isVisible());

        ArgumentCaptor<com.trackfindergarage.backend.domain.model.EventBookingService> captor =
                ArgumentCaptor.forClass(com.trackfindergarage.backend.domain.model.EventBookingService.class);
        verify(eventBookingServicePersistencePort, times(2)).save(captor.capture());
        assertEquals(List.of(new BigDecimal("20.00"), new BigDecimal("30.00")),
                captor.getAllValues().stream().map(com.trackfindergarage.backend.domain.model.EventBookingService::getPriceAtPurchase).toList());
    }

    @Test
    void checkoutEventBookingRejectsNonStandardUsers() {
        User organizerUser = user(1L, "organizer", "ORGANIZER");
        List<Long> eventServiceIds = List.of();

        when(userPersistencePort.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizerUser));

        assertThrows(
                AccessDeniedException.class,
                () -> eventBookingService.checkoutEventBooking("organizer@example.com", 5L, eventServiceIds, true)
        );
        verify(eventBookingPersistencePort, never()).save(any(EventBooking.class));
    }

    @Test
    void checkoutEventBookingRejectsDuplicateBookings() {
        User user = user(1L, "driver", "USER");
        Event event = event(5L, LocalDate.now().plusDays(20), 10);
        EventBooking existingBooking = booking(80L, user, event, true);
        List<Long> eventServiceIds = List.of();

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(eventPersistencePort.findById(5L)).thenReturn(Optional.of(event));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 5L)).thenReturn(Optional.of(existingBooking));

        assertThrows(
                DuplicateResourceException.class,
                () -> eventBookingService.checkoutEventBooking("driver@example.com", 5L, eventServiceIds, true)
        );
    }

    @Test
    void checkoutEventBookingRejectsFullEvents() {
        User user = user(1L, "driver", "USER");
        Event event = event(5L, LocalDate.now().plusDays(20), 1);
        List<Long> eventServiceIds = List.of();

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(eventPersistencePort.findById(5L)).thenReturn(Optional.of(event));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 5L)).thenReturn(Optional.empty());
        when(eventBookingPersistencePort.countByEventId(5L)).thenReturn(1L);

        assertThrows(
                ConflictException.class,
                () -> eventBookingService.checkoutEventBooking("driver@example.com", 5L, eventServiceIds, true)
        );
    }

    @Test
    void checkoutEventBookingRejectsServicesFromAnotherEvent() {
        User user = user(1L, "driver", "USER");
        Event targetEvent = event(5L, LocalDate.now().plusDays(20), 10);
        Event otherEvent = event(9L, LocalDate.now().plusDays(30), 10);
        EventService wrongService = eventService(11L, otherEvent, "25.00");
        List<Long> eventServiceIds = List.of(11L);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(eventPersistencePort.findById(5L)).thenReturn(Optional.of(targetEvent));
        when(eventBookingPersistencePort.findByUserIdAndEventId(1L, 5L)).thenReturn(Optional.empty());
        when(eventBookingPersistencePort.countByEventId(5L)).thenReturn(0L);
        when(eventBookingPersistencePort.save(any(EventBooking.class))).then(returnsFirstArg());
        when(eventServicePersistencePort.findById(11L)).thenReturn(Optional.of(wrongService));

        assertThrows(
                IllegalArgumentException.class,
                () -> eventBookingService.checkoutEventBooking("driver@example.com", 5L, eventServiceIds, true)
        );
        verify(eventBookingServicePersistencePort, never())
                .save(any(com.trackfindergarage.backend.domain.model.EventBookingService.class));
    }

    @Test
    void updateOwnEventBookingVisibilityPersistsWhenBookingBelongsToAuthenticatedUser() {
        User user = user(1L, "driver", "USER");
        EventBooking existingBooking = booking(80L, bookingOwner(1L, "DRIVER@example.com"), event(5L, LocalDate.now().plusDays(20), 10), true);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(eventBookingPersistencePort.findById(80L)).thenReturn(Optional.of(existingBooking));
        when(eventBookingPersistencePort.save(existingBooking)).thenReturn(existingBooking);

        EventBooking updatedBooking = eventBookingService.updateOwnEventBookingVisibility(" driver@example.com ", 80L, false);

        assertEquals(false, updatedBooking.isVisible());
        verify(eventBookingPersistencePort).save(existingBooking);
    }

    @Test
    void deleteOwnEventBookingDeletesAttachedServicesAndBooking() {
        User user = user(1L, "driver", "USER");
        EventBooking existingBooking = booking(80L, bookingOwner(1L, "driver@example.com"), event(5L, LocalDate.now().plusDays(20), 10), true);
        com.trackfindergarage.backend.domain.model.EventBookingService firstBookedService =
                bookedService(1L, existingBooking, eventService(11L, existingBooking.getEvent(), "20.00"));
        com.trackfindergarage.backend.domain.model.EventBookingService secondBookedService =
                bookedService(2L, existingBooking, eventService(12L, existingBooking.getEvent(), "30.00"));

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(eventBookingPersistencePort.findById(80L)).thenReturn(Optional.of(existingBooking));
        when(eventBookingServicePersistencePort.findByEventBookingId(80L))
                .thenReturn(List.of(firstBookedService, secondBookedService));

        eventBookingService.deleteOwnEventBooking("driver@example.com", 80L);

        verify(eventBookingServicePersistencePort).delete(firstBookedService);
        verify(eventBookingServicePersistencePort).delete(secondBookedService);
        verify(eventBookingPersistencePort).delete(existingBooking);
    }

    @Test
    void deleteOwnEventBookingRejectsBookingsThatAreTooCloseToTheEvent() {
        User user = user(1L, "driver", "USER");
        EventBooking existingBooking = booking(80L, bookingOwner(1L, "driver@example.com"), event(5L, LocalDate.now().plusDays(10), 10), true);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(eventBookingPersistencePort.findById(80L)).thenReturn(Optional.of(existingBooking));

        assertThrows(
                IllegalArgumentException.class,
                () -> eventBookingService.deleteOwnEventBooking("driver@example.com", 80L)
        );
        verify(eventBookingPersistencePort, never()).delete(existingBooking);
    }

    @Test
    void deleteOwnEventBookingRejectsBookingsFromOtherUsers() {
        User user = user(1L, "driver", "USER");
        EventBooking existingBooking = booking(80L, bookingOwner(2L, "other@example.com"), event(5L, LocalDate.now().plusDays(20), 10), true);

        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(eventBookingPersistencePort.findById(80L)).thenReturn(Optional.of(existingBooking));

        assertThrows(
                AccessDeniedException.class,
                () -> eventBookingService.deleteOwnEventBooking("driver@example.com", 80L)
        );
    }

    @Test
    void getEventBookingsByUserIdReturnsOnlyVisibleBookings() {
        User user = user(1L, "driver", "USER");
        Event event = event(5L, LocalDate.now().plusDays(20), 10);
        EventBooking visibleBooking = booking(80L, user, event, true);
        EventBooking hiddenBooking = booking(81L, user, event, false);

        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(eventBookingPersistencePort.findByUserId(1L)).thenReturn(List.of(visibleBooking, hiddenBooking));

        List<EventBooking> bookings = eventBookingService.getEventBookingsByUserId(1L);

        assertEquals(List.of(visibleBooking), bookings);
    }

    private User user(Long id, String displayName, String roleName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setEmail(displayName.toLowerCase() + "@example.com");

        Role role = new Role();
        role.setId("USER".equals(roleName) ? 1L : 2L);
        role.setRoleName(roleName);
        user.setRole(role);
        return user;
    }

    private User bookingOwner(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private Event event(Long id, LocalDate eventDate, Integer maxParticipants) {
        Track track = new Track();
        track.setId(7L);
        track.setName("Jarama");

        Organizer organizer = new Organizer();
        organizer.setIdUser(3L);
        organizer.setLegalName("Track Events");

        Event event = new Event();
        event.setId(id);
        event.setTrack(track);
        event.setOrganizer(organizer);
        event.setEventDate(eventDate);
        event.setBasePrice(new BigDecimal("120.00"));
        event.setMaxParticipants(maxParticipants);
        return event;
    }

    private EventService eventService(Long id, Event event, String price) {
        EventService eventService = new EventService();
        eventService.setId(id);
        eventService.setEvent(event);
        eventService.setPrice(new BigDecimal(price));
        return eventService;
    }

    private EventBooking booking(Long id, User user, Event event, boolean visible) {
        EventBooking booking = new EventBooking();
        booking.setId(id);
        booking.setUser(user);
        booking.setEvent(event);
        booking.setVisible(visible);
        booking.setBasePriceAtPurchase(event.getBasePrice());
        return booking;
    }

    private com.trackfindergarage.backend.domain.model.EventBookingService bookedService(Long id,
                                                                                         EventBooking booking,
                                                                                         EventService eventService) {
        com.trackfindergarage.backend.domain.model.EventBookingService bookedService =
                new com.trackfindergarage.backend.domain.model.EventBookingService();
        bookedService.setId(id);
        bookedService.setEventBooking(booking);
        bookedService.setEventService(eventService);
        bookedService.setPriceAtPurchase(eventService.getPrice());
        return bookedService;
    }
}
