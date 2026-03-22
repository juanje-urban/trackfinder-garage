package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventUseCase;
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

    private static final String EVENT_NOT_FOUND_WITH_ID = "Event not found with id: ";
    private static final String ORGANIZER_NOT_FOUND_WITH_ID = "Organizer not found with id: ";
    private static final String TRACK_NOT_FOUND_WITH_ID = "Track not found with id: ";
    private static final String EVENT_ALREADY_EXISTS_FOR_TRACK_AND_DATE =
            "An event already exists for track id %d on date %s";
    private static final String ORGANIZER_ID_REQUIRED = "Organizer id is required";
    private static final String TRACK_ID_REQUIRED = "Track id is required";
    private static final String EVENT_DATE_REQUIRED = "Event date is required";
    private static final String EVENT_DATE_MUST_BE_FUTURE = "Event date must be in the future";
    private static final String BASE_PRICE_REQUIRED = "Base price is required";
    private static final String BASE_PRICE_MUST_BE_GREATER_THAN_ZERO = "Base price must be greater than 0";
    private static final String START_DATE_REQUIRED = "Start date is required";
    private static final String END_DATE_REQUIRED = "End date is required";
    private static final String START_DATE_MUST_BE_BEFORE_OR_EQUAL_END_DATE =
            "Start date must be before or equal to end date";

    private final EventPersistencePort eventPersistencePort;
    private final OrganizerPersistencePort organizerPersistencePort;
    private final TrackPersistencePort trackPersistencePort;

    public EventService(EventPersistencePort eventPersistencePort,
                        OrganizerPersistencePort organizerPersistencePort,
                        TrackPersistencePort trackPersistencePort) {
        this.eventPersistencePort = eventPersistencePort;
        this.organizerPersistencePort = organizerPersistencePort;
        this.trackPersistencePort = trackPersistencePort;
    }

    @Override
    public Event createEvent(Event event) {
        validateEvent(event);

        Long organizerId = extractOrganizerId(event);
        Long trackId = extractTrackId(event);

        Organizer organizer = organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + organizerId));
        Track track = trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        validateTrackAndDateUniqueness(trackId, event.getEventDate(), null);

        event.setOrganizer(organizer);
        event.setTrack(track);

        return eventPersistencePort.save(event);
    }

    @Override
    public Event updateEvent(Long id, Event event) {
        validateEvent(event);

        Event existingEvent = findEventOrThrow(id);

        if (!existingEvent.getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Past events cannot be modified");
        }

        Long organizerId = extractOrganizerId(event);
        Long trackId = extractTrackId(event);

        Organizer organizer = organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + organizerId));
        Track track = trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        validateTrackAndDateUniqueness(trackId, event.getEventDate(), id);

        existingEvent.setOrganizer(organizer);
        existingEvent.setTrack(track);
        existingEvent.setEventDate(event.getEventDate());
        existingEvent.setBasePrice(event.getBasePrice());

        return eventPersistencePort.save(existingEvent);
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = findEventOrThrow(id);
        eventPersistencePort.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventPersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByOrganizerId(Long organizerId) {
        organizerPersistencePort.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_WITH_ID + organizerId));

        return eventPersistencePort.findByOrganizerIdUser(organizerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByTrackId(Long trackId) {
        trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));

        return eventPersistencePort.findByTrackId(trackId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return eventPersistencePort.findByEventDateBetween(startDate, endDate);
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
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException(START_DATE_REQUIRED);
        }
        if (endDate == null) {
            throw new IllegalArgumentException(END_DATE_REQUIRED);
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(START_DATE_MUST_BE_BEFORE_OR_EQUAL_END_DATE);
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

    private Long extractOrganizerId(Event event) {
        return event.getOrganizer().getIdUser();
    }

    private Long extractTrackId(Event event) {
        return event.getTrack().getId();
    }

    private Event findEventOrThrow(Long id) {
        return eventPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + id));
    }
}
