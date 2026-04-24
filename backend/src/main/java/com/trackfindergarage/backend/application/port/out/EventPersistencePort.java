package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventPersistencePort {

    Event save(Event event);

    Optional<Event> findById(Long id);

    List<Event> findFutureEvents(LocalDate fromDate);

    List<Event> findByOrganizerIdUser(Long organizerId);

    Optional<Event> findByTrackIdAndEventDate(Long trackId, LocalDate eventDate);

    void delete(Event event);
}
