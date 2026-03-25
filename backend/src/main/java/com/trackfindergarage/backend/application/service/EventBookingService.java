package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventBookingUseCase;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ConflictException;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class EventBookingService implements EventBookingUseCase {

    private static final String EVENT_BOOKING_NOT_FOUND_WITH_ID = "Event booking not found with id: ";
    private static final String USER_NOT_FOUND_WITH_ID = "User not found with id: ";
    private static final String EVENT_NOT_FOUND_WITH_ID = "Event not found with id: ";
    private static final String USER_ID_REQUIRED = "User id is required";
    private static final String EVENT_ID_REQUIRED = "Event id is required";
    private static final String EVENT_BOOKING_ALREADY_EXISTS =
            "User id %d already has a booking for event id %d";
    private static final String EVENT_BOOKING_REQUIRES_FUTURE_EVENT =
            "Event booking can only be created for future events";
    private static final String EVENT_BOOKING_REQUIRES_EVENT_CAPACITY =
            "Event max participants is required before accepting bookings";
    private static final String EVENT_IS_FULL = "Event id %d is full";
    private static final String EVENT_BOOKING_CANNOT_BE_DELETED_WITHIN_14_DAYS =
            "Event booking cannot be deleted less than 14 days before the event";

    private final EventBookingPersistencePort eventBookingPersistencePort;
    private final EventBookingServicePersistencePort eventBookingServicePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final EventPersistencePort eventPersistencePort;

    public EventBookingService(EventBookingPersistencePort eventBookingPersistencePort,
                               EventBookingServicePersistencePort eventBookingServicePersistencePort,
                               UserPersistencePort userPersistencePort,
                               EventPersistencePort eventPersistencePort) {
        this.eventBookingPersistencePort = eventBookingPersistencePort;
        this.eventBookingServicePersistencePort = eventBookingServicePersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.eventPersistencePort = eventPersistencePort;
    }

    @Override
    public EventBooking createEventBooking(EventBooking eventBooking) {
        validateEventBooking(eventBooking);

        Long userId = extractUserId(eventBooking);
        Long eventId = extractEventId(eventBooking);

        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));
        Event event = eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));

        if (!event.getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(EVENT_BOOKING_REQUIRES_FUTURE_EVENT);
        }

        eventBookingPersistencePort.findByUserIdAndEventId(userId, eventId)
                .ifPresent(existingEventBooking -> {
                    throw new DuplicateResourceException(EVENT_BOOKING_ALREADY_EXISTS.formatted(userId, eventId));
                });

        if (event.getMaxParticipants() == null) {
            throw new IllegalArgumentException(EVENT_BOOKING_REQUIRES_EVENT_CAPACITY);
        }

        if (eventBookingPersistencePort.countByEventId(eventId) >= event.getMaxParticipants()) {
            throw new ConflictException(EVENT_IS_FULL.formatted(eventId));
        }

        eventBooking.setUser(user);
        eventBooking.setEvent(event);
        eventBooking.setBookedAt(LocalDateTime.now());
        eventBooking.setBasePriceAtPurchase(event.getBasePrice());

        return eventBookingPersistencePort.save(eventBooking);
    }

    @Override
    public void deleteEventBooking(Long id) {
        EventBooking eventBooking = findEventBookingOrThrow(id);

        LocalDate deletionLimitDate = LocalDate.now().plusDays(14);
        if (eventBooking.getEvent().getEventDate().isBefore(deletionLimitDate)) {
            throw new IllegalArgumentException(EVENT_BOOKING_CANNOT_BE_DELETED_WITHIN_14_DAYS);
        }

        eventBookingServicePersistencePort.findByEventBookingId(id)
                .forEach(eventBookingServicePersistencePort::delete);

        eventBookingPersistencePort.delete(eventBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBooking> getAllEventBookings() {
        return eventBookingPersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public EventBooking getEventBookingById(Long id) {
        return findEventBookingOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBooking> getEventBookingsByUserId(Long userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));

        return eventBookingPersistencePort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBooking> getEventBookingsByEventId(Long eventId) {
        eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));

        return eventBookingPersistencePort.findByEventId(eventId);
    }

    private void validateEventBooking(EventBooking eventBooking) {
        if (eventBooking.getUser() == null || eventBooking.getUser().getId() == null) {
            throw new IllegalArgumentException(USER_ID_REQUIRED);
        }
        if (eventBooking.getEvent() == null || eventBooking.getEvent().getId() == null) {
            throw new IllegalArgumentException(EVENT_ID_REQUIRED);
        }
    }

    private Long extractUserId(EventBooking eventBooking) {
        return eventBooking.getUser().getId();
    }

    private Long extractEventId(EventBooking eventBooking) {
        return eventBooking.getEvent().getId();
    }

    private EventBooking findEventBookingOrThrow(Long id) {
        return eventBookingPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_BOOKING_NOT_FOUND_WITH_ID + id));
    }
}
