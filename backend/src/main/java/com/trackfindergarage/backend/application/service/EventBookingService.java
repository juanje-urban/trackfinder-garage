package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventBookingUseCase;
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
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "Authenticated user email is required";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
    private static final String EVENT_SERVICE_NOT_FOUND_WITH_ID = "Event service not found with id: ";
    private static final String EVENT_SERVICE_MUST_BELONG_TO_EVENT =
            "Event service id %d does not belong to event id %d";
    private static final String ONLY_STANDARD_USERS_CAN_BOOK_EVENTS =
            "Only standard users can book events";
    private static final String USER_ROLE_NAME = "USER";
    private static final String ONLY_BOOKING_OWNER_CAN_CANCEL_EVENT_BOOKING =
            "Only the owner of the booking can cancel it";

    private final EventBookingPersistencePort eventBookingPersistencePort;
    private final EventBookingServicePersistencePort eventBookingServicePersistencePort;
    private final EventServicePersistencePort eventServicePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final EventPersistencePort eventPersistencePort;

    public EventBookingService(EventBookingPersistencePort eventBookingPersistencePort,
                               EventBookingServicePersistencePort eventBookingServicePersistencePort,
                               EventServicePersistencePort eventServicePersistencePort,
                               UserPersistencePort userPersistencePort,
                               EventPersistencePort eventPersistencePort) {
        this.eventBookingPersistencePort = eventBookingPersistencePort;
        this.eventBookingServicePersistencePort = eventBookingServicePersistencePort;
        this.eventServicePersistencePort = eventServicePersistencePort;
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
    public EventBooking checkoutEventBooking(String authenticatedEmail, Long eventId, List<Long> eventServiceIds) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        User user = userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail));

        if (user.getRole() == null
                || user.getRole().getRoleName() == null
                || !USER_ROLE_NAME.equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new AccessDeniedException(ONLY_STANDARD_USERS_CAN_BOOK_EVENTS);
        }

        EventBooking eventBooking = new EventBooking();
        eventBooking.setUser(user);

        Event event = new Event();
        event.setId(eventId);
        eventBooking.setEvent(event);

        EventBooking createdEventBooking = createEventBooking(eventBooking);

        List<Long> distinctEventServiceIds = eventServiceIds == null
                ? List.of()
                : eventServiceIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        distinctEventServiceIds.forEach(eventServiceId ->
                createEventBookingServiceForCheckout(createdEventBooking, eventServiceId, eventId));

        return createdEventBooking;
    }

    @Override
    public void deleteEventBooking(Long id) {
        deleteEventBooking(findEventBookingOrThrow(id));
    }

    @Override
    public void deleteOwnEventBooking(String authenticatedEmail, Long id) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        EventBooking eventBooking = findEventBookingOrThrow(id);
        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);

        if (eventBooking.getUser() == null
                || eventBooking.getUser().getEmail() == null
                || !normalizedEmail.equals(eventBooking.getUser().getEmail().trim().toLowerCase(Locale.ROOT))) {
            throw new AccessDeniedException(ONLY_BOOKING_OWNER_CAN_CANCEL_EVENT_BOOKING);
        }

        deleteEventBooking(eventBooking);
    }

    private void deleteEventBooking(EventBooking eventBooking) {
        LocalDate deletionLimitDate = LocalDate.now().plusDays(14);
        if (eventBooking.getEvent().getEventDate().isBefore(deletionLimitDate)) {
            throw new IllegalArgumentException(EVENT_BOOKING_CANNOT_BE_DELETED_WITHIN_14_DAYS);
        }

        eventBookingServicePersistencePort.findByEventBookingId(eventBooking.getId())
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
    public List<EventBooking> getEventBookingsByAuthenticatedEmail(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        User user = userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail));

        return eventBookingPersistencePort.findByUserId(user.getId());
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

    private void createEventBookingServiceForCheckout(EventBooking eventBooking, Long eventServiceId, Long eventId) {
        EventService eventService = eventServicePersistencePort.findById(eventServiceId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_SERVICE_NOT_FOUND_WITH_ID + eventServiceId));

        if (eventService.getEvent() == null || !eventId.equals(eventService.getEvent().getId())) {
            throw new IllegalArgumentException(EVENT_SERVICE_MUST_BELONG_TO_EVENT.formatted(eventServiceId, eventId));
        }

        com.trackfindergarage.backend.domain.model.EventBookingService eventBookingService =
                new com.trackfindergarage.backend.domain.model.EventBookingService();
        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(eventService);
        eventBookingService.setPriceAtPurchase(eventService.getPrice());

        eventBookingServicePersistencePort.save(eventBookingService);
    }
}
