package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventBookingServiceUseCase;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@org.springframework.stereotype.Service
@Transactional
public class EventBookingServiceService implements EventBookingServiceUseCase {

    private static final String EVENT_BOOKING_SERVICE_NOT_FOUND_WITH_ID = "Servicio de reserva de evento no encontrado con id: ";
    private static final String EVENT_BOOKING_NOT_FOUND_WITH_ID = "Reserva de evento no encontrada con id: ";
    private static final String EVENT_SERVICE_NOT_FOUND_WITH_ID = "Servicio de evento no encontrado con id: ";
    private static final String EVENT_NOT_FOUND_WITH_ID = "Evento no encontrado con id: ";
    private static final String USER_NOT_FOUND_WITH_ID = "Usuario no encontrado con id: ";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "Usuario no encontrado con correo electrónico: ";
    private static final String EVENT_BOOKING_ID_REQUIRED = "El id de la reserva de evento es obligatorio";
    private static final String EVENT_SERVICE_ID_REQUIRED = "El id del servicio de evento es obligatorio";
    private static final String EVENT_BOOKING_SERVICE_ALREADY_EXISTS =
            "El servicio de evento con id %d ya está asociado a la reserva de evento con id %d";
    private static final String EVENT_SERVICE_MUST_BELONG_TO_BOOKING_EVENT =
            "El servicio de evento con id %d no pertenece al mismo evento que la reserva de evento con id %d";
    private static final String EVENT_BOOKING_SERVICE_REQUIRES_FUTURE_EVENT =
            "El servicio de reserva de evento solo puede crearse para eventos futuros";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "El correo electrónico del usuario autenticado es obligatorio";

    private final EventBookingServicePersistencePort eventBookingServicePersistencePort;
    private final EventBookingPersistencePort eventBookingPersistencePort;
    private final EventServicePersistencePort eventServicePersistencePort;
    private final EventPersistencePort eventPersistencePort;
    private final UserPersistencePort userPersistencePort;

    public EventBookingServiceService(EventBookingServicePersistencePort eventBookingServicePersistencePort,
                                      EventBookingPersistencePort eventBookingPersistencePort,
                                      EventServicePersistencePort eventServicePersistencePort,
                                      EventPersistencePort eventPersistencePort,
                                      UserPersistencePort userPersistencePort) {
        this.eventBookingServicePersistencePort = eventBookingServicePersistencePort;
        this.eventBookingPersistencePort = eventBookingPersistencePort;
        this.eventServicePersistencePort = eventServicePersistencePort;
        this.eventPersistencePort = eventPersistencePort;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public EventBookingService createEventBookingService(EventBookingService eventBookingService) {
        validateEventBookingService(eventBookingService);

        Long eventBookingId = extractEventBookingId(eventBookingService);
        Long eventServiceId = extractEventServiceId(eventBookingService);

        EventBooking eventBooking = eventBookingPersistencePort.findById(eventBookingId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_BOOKING_NOT_FOUND_WITH_ID + eventBookingId));
        EventService eventService = eventServicePersistencePort.findById(eventServiceId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_SERVICE_NOT_FOUND_WITH_ID + eventServiceId));

        if (!eventBooking.getEvent().getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(EVENT_BOOKING_SERVICE_REQUIRES_FUTURE_EVENT);
        }
        if (!eventService.getEvent().getId().equals(eventBooking.getEvent().getId())) {
            throw new IllegalArgumentException(
                    EVENT_SERVICE_MUST_BELONG_TO_BOOKING_EVENT.formatted(eventServiceId, eventBookingId)
            );
        }

        eventBookingServicePersistencePort.findByEventBookingIdAndEventServiceId(eventBookingId, eventServiceId)
                .ifPresent(existingEventBookingService -> {
                    throw new DuplicateResourceException(
                            EVENT_BOOKING_SERVICE_ALREADY_EXISTS.formatted(eventServiceId, eventBookingId)
                    );
                });

        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(eventService);
        eventBookingService.setPriceAtPurchase(eventService.getPrice());

        return eventBookingServicePersistencePort.save(eventBookingService);
    }

    @Override
    public void deleteEventBookingService(Long id) {
        EventBookingService eventBookingService = findEventBookingServiceOrThrow(id);
        eventBookingServicePersistencePort.delete(eventBookingService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBookingService> getAllEventBookingServices() {
        return eventBookingServicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public EventBookingService getEventBookingServiceById(Long id) {
        return findEventBookingServiceOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBookingService> getEventBookingServicesByEventBookingId(Long eventBookingId) {
        eventBookingPersistencePort.findById(eventBookingId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_BOOKING_NOT_FOUND_WITH_ID + eventBookingId));

        return eventBookingServicePersistencePort.findByEventBookingId(eventBookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBookingService> getEventBookingServicesByEventId(Long eventId) {
        eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));

        return eventBookingServicePersistencePort.findByEventBookingEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBookingService> getEventBookingServicesByEventIdAndAuthenticatedEmail(Long eventId,
                                                                                            String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));

        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        Long userId = userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail))
                .getId();

        return eventBookingServicePersistencePort.findByEventBookingEventIdAndEventBookingUserId(eventId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBookingService> getEventBookingServicesByUserId(Long userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));

        return eventBookingServicePersistencePort.findByEventBookingUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBookingService> getEventBookingServicesByEventIdAndUserId(Long eventId, Long userId) {
        eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));

        return eventBookingServicePersistencePort.findByEventBookingEventIdAndEventBookingUserId(eventId, userId);
    }

    private void validateEventBookingService(EventBookingService eventBookingService) {
        if (eventBookingService.getEventBooking() == null || eventBookingService.getEventBooking().getId() == null) {
            throw new IllegalArgumentException(EVENT_BOOKING_ID_REQUIRED);
        }
        if (eventBookingService.getEventService() == null || eventBookingService.getEventService().getId() == null) {
            throw new IllegalArgumentException(EVENT_SERVICE_ID_REQUIRED);
        }
    }

    private Long extractEventBookingId(EventBookingService eventBookingService) {
        return eventBookingService.getEventBooking().getId();
    }

    private Long extractEventServiceId(EventBookingService eventBookingService) {
        return eventBookingService.getEventService().getId();
    }

    private EventBookingService findEventBookingServiceOrThrow(Long id) {
        return eventBookingServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_BOOKING_SERVICE_NOT_FOUND_WITH_ID + id));
    }
}
