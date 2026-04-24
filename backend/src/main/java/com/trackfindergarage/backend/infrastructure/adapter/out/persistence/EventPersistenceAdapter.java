package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.domain.model.Event;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class EventPersistenceAdapter implements EventPersistencePort {

    private final SpringDataEventRepository eventRepository;

    public EventPersistenceAdapter(SpringDataEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> findFutureEvents(LocalDate fromDate) {
        return eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAsc(fromDate);
    }

    @Override
    public List<Event> findByOrganizerIdUser(Long organizerId) {
        return eventRepository.findByOrganizerIdUser(organizerId);
    }

    @Override
    public Optional<Event> findByTrackIdAndEventDate(Long trackId, LocalDate eventDate) {
        return eventRepository.findByTrackIdAndEventDate(trackId, eventDate);
    }

    @Override
    public void delete(Event event) {
        eventRepository.delete(event);
    }
}
