package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.EventService;

import java.util.List;

public interface EventServiceUseCase {

    EventService createEventService(EventService eventService);

    EventService updateEventService(Long id, EventService eventService);

    void deleteEventService(Long id);

    List<EventService> getEventServicesByEventId(Long eventId);
}
