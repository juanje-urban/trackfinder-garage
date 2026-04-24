package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventUseCase;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class EventService implements EventUseCase {

    private static final String EVENT_NOT_FOUND_WITH_ID = "Evento no encontrado con id: ";
    private static final String ORGANIZER_NOT_FOUND_WITH_ID = "Organizador no encontrado con id: ";
    private static final String TRACK_NOT_FOUND_WITH_ID = "Circuito no encontrado con id: ";
    private static final String EVENT_ALREADY_EXISTS_FOR_TRACK_AND_DATE =
            "Ya existe un evento para el circuito con id %d en la fecha %s";
    private static final String ORGANIZER_ID_REQUIRED = "El id del organizador es obligatorio";
    private static final String TRACK_ID_REQUIRED = "El id del circuito es obligatorio";
    private static final String EVENT_DATE_REQUIRED = "La fecha del evento es obligatoria";
    private static final String EVENT_DATE_MUST_BE_FUTURE = "La fecha del evento debe ser futura";
    private static final String BASE_PRICE_REQUIRED = "El precio base es obligatorio";
    private static final String BASE_PRICE_MUST_BE_GREATER_THAN_ZERO = "El precio base debe ser mayor que 0";
    private static final String MAX_PARTICIPANTS_REQUIRED = "El número máximo de participantes es obligatorio";
    private static final String MAX_PARTICIPANTS_MUST_BE_GREATER_THAN_ZERO = "El número máximo de participantes debe ser mayor que 0";
    private static final String DESCRIPTION_REQUIRED = "La descripción es obligatoria";
    private static final String MAX_PARTICIPANTS_CANNOT_BE_LESS_THAN_CURRENT_BOOKINGS =
            "El número máximo de participantes no puede ser menor que el número actual de reservas (%d)";

    private final EventBookingPersistencePort eventBookingPersistencePort;
    private final EventPersistencePort eventPersistencePort;
    private final OrganizerPersistencePort organizerPersistencePort;
    private final TrackPersistencePort trackPersistencePort;

    public EventService(EventBookingPersistencePort eventBookingPersistencePort,
                        EventPersistencePort eventPersistencePort,
                        OrganizerPersistencePort organizerPersistencePort,
                        TrackPersistencePort trackPersistencePort) {
        this.eventBookingPersistencePort = eventBookingPersistencePort;
        this.eventPersistencePort = eventPersistencePort;
        this.organizerPersistencePort = organizerPersistencePort;
        this.trackPersistencePort = trackPersistencePort;
    }

    @Override
    public Event createEvent(Event event) {
        validateEvent(event);

        Long organizerId = event.getOrganizer().getIdUser();
        Long trackId = event.getTrack().getId();

        Organizer organizer = organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + organizerId));
        Track track = trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        validateTrackAndDateUniqueness(trackId, event.getEventDate(), null);

        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setDescription(event.getDescription().trim());

        return eventPersistencePort.save(event);
    }

    @Override
    public Event updateEvent(Long id, Event event) {
        validateEvent(event);

        Event existingEvent = findEventOrThrow(id);

        if (!existingEvent.getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Past events cannot be modified");
        }

        Long organizerId = event.getOrganizer().getIdUser();
        Long trackId = event.getTrack().getId();

        Organizer organizer = organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + organizerId));
        Track track = trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        validateTrackAndDateUniqueness(trackId, event.getEventDate(), id);
        validateMaxParticipantsAgainstBookings(id, event.getMaxParticipants());

        existingEvent.setOrganizer(organizer);
        existingEvent.setTrack(track);
        existingEvent.setEventDate(event.getEventDate());
        existingEvent.setBasePrice(event.getBasePrice());
        existingEvent.setMaxParticipants(event.getMaxParticipants());
        existingEvent.setDescription(event.getDescription().trim());

        return eventPersistencePort.save(existingEvent);
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = findEventOrThrow(id);
        eventPersistencePort.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getFutureEvents() {
        return eventPersistencePort.findFutureEvents(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + id));
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingCapacity(Long eventId) {
        Event event = findEventOrThrow(eventId);

        int currentBookings = Math.toIntExact(eventBookingPersistencePort.countByEventId(eventId));
        return Math.max(0, event.getMaxParticipants() - currentBookings);
    }

    private void validateEvent(Event event) {
        if (event.getOrganizer() == null || event.getOrganizer().getIdUser() == null) {
            throw new IllegalArgumentException(ORGANIZER_ID_REQUIRED);
        }
        if (event.getTrack() == null || event.getTrack().getId() == null) {
            throw new IllegalArgumentException(TRACK_ID_REQUIRED);
        }
        if (event.getEventDate() == null) {
            throw new IllegalArgumentException(EVENT_DATE_REQUIRED);
        }
        if (!event.getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(EVENT_DATE_MUST_BE_FUTURE);
        }
        if (event.getBasePrice() == null) {
            throw new IllegalArgumentException(BASE_PRICE_REQUIRED);
        }
        if (event.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(BASE_PRICE_MUST_BE_GREATER_THAN_ZERO);
        }
        if (event.getMaxParticipants() == null) {
            throw new IllegalArgumentException(MAX_PARTICIPANTS_REQUIRED);
        }
        if (event.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException(MAX_PARTICIPANTS_MUST_BE_GREATER_THAN_ZERO);
        }
        if (event.getDescription() == null || event.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException(DESCRIPTION_REQUIRED);
        }
    }

    private void validateTrackAndDateUniqueness(Long trackId, LocalDate eventDate, Long currentEventId) {
        eventPersistencePort.findByTrackIdAndEventDate(trackId, eventDate)
                .ifPresent(existingEvent -> {
                    if (currentEventId == null || !existingEvent.getId().equals(currentEventId)) {
                        throw new DuplicateResourceException(
                                EVENT_ALREADY_EXISTS_FOR_TRACK_AND_DATE.formatted(trackId, eventDate)
                        );
                    }
                });
    }

    private void validateMaxParticipantsAgainstBookings(Long eventId, Integer maxParticipants) {
        int currentBookings = Math.toIntExact(eventBookingPersistencePort.countByEventId(eventId));

        if (maxParticipants < currentBookings) {
            throw new IllegalArgumentException(
                    MAX_PARTICIPANTS_CANNOT_BE_LESS_THAN_CURRENT_BOOKINGS.formatted(currentBookings)
            );
        }
    }

    private Event findEventOrThrow(Long id) {
        return eventPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + id));
    }
}
