package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Event;

import java.util.List;

public interface EventUseCase {

    Event createEvent(Event event);

    Event updateEvent(Long id, Event event);

    void deleteEvent(Long id);

    List<Event> getFutureEvents();

    Event getEventById(Long id);

    int getRemainingCapacity(Long eventId);

}
