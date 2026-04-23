package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventBookingServiceServiceTest {

    @Mock
    private EventBookingServicePersistencePort eventBookingServicePersistencePort;

    @Mock
    private EventBookingPersistencePort eventBookingPersistencePort;

    @Mock
    private EventPersistencePort eventPersistencePort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @InjectMocks
    private EventBookingServiceService eventBookingServiceService;

    @Test
    void getEventBookingServicesByEventBookingIdReturnsServicesForExistingBooking() {
        EventBooking booking = new EventBooking();
        booking.setId(7L);
        List<EventBookingService> bookedServices = List.of(eventBookingService(11L, 7L, 3L));

        when(eventBookingPersistencePort.findById(7L)).thenReturn(Optional.of(booking));
        when(eventBookingServicePersistencePort.findByEventBookingId(7L)).thenReturn(bookedServices);

        assertEquals(bookedServices, eventBookingServiceService.getEventBookingServicesByEventBookingId(7L));
    }

    @Test
    void getEventBookingServicesByEventBookingIdThrowsWhenBookingDoesNotExist() {
        when(eventBookingPersistencePort.findById(7L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventBookingServiceService.getEventBookingServicesByEventBookingId(7L)
        );
    }

    @Test
    void getEventBookingServicesByEventIdAndAuthenticatedEmailNormalizesEmail() {
        Event event = new Event();
        event.setId(9L);

        User user = new User();
        user.setId(4L);
        user.setEmail("driver@example.com");

        List<EventBookingService> bookedServices = List.of(eventBookingService(12L, 8L, 9L));

        when(eventPersistencePort.findById(9L)).thenReturn(Optional.of(event));
        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(eventBookingServicePersistencePort.findByEventBookingEventIdAndEventBookingUserId(9L, 4L))
                .thenReturn(bookedServices);

        assertEquals(
                bookedServices,
                eventBookingServiceService.getEventBookingServicesByEventIdAndAuthenticatedEmail(9L, " DRIVER@example.com ")
        );
    }

    @Test
    void getEventBookingServicesByEventIdAndAuthenticatedEmailThrowsWhenEmailIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> eventBookingServiceService.getEventBookingServicesByEventIdAndAuthenticatedEmail(9L, " ")
        );
    }

    @Test
    void getEventBookingServicesByEventIdAndAuthenticatedEmailThrowsWhenEventDoesNotExist() {
        when(eventPersistencePort.findById(9L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventBookingServiceService.getEventBookingServicesByEventIdAndAuthenticatedEmail(9L, "driver@example.com")
        );
    }

    @Test
    void getEventBookingServicesByEventIdAndAuthenticatedEmailThrowsWhenUserDoesNotExist() {
        Event event = new Event();
        event.setId(9L);

        when(eventPersistencePort.findById(9L)).thenReturn(Optional.of(event));
        when(userPersistencePort.findByEmail("driver@example.com")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventBookingServiceService.getEventBookingServicesByEventIdAndAuthenticatedEmail(9L, "driver@example.com")
        );
    }

    private EventBookingService eventBookingService(Long id, Long bookingId, Long eventId) {
        User user = new User();
        user.setId(4L);

        Event event = new Event();
        event.setId(eventId);

        EventBooking booking = new EventBooking();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setEvent(event);

        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setId(id);
        eventBookingService.setEventBooking(booking);
        return eventBookingService;
    }
}
