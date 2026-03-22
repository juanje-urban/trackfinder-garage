package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface EventUseCase {

    Event createEvent(Event event);

    Event updateEvent(Long id, Event event);

    void deleteEvent(Long id);

    List<Event> getAllEvents();

    Event getEventById(Long id);

    List<Event> getEventsByOrganizerId(Long organizerId);

    List<Event> getEventsByTrackId(Long trackId);

    List<Event> getEventsByDateRange(LocalDate startDate, LocalDate endDate);
}
