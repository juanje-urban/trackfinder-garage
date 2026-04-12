package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.domain.model.EventService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EventServicePersistenceAdapter implements EventServicePersistencePort {

    private final SpringDataEventServiceRepository eventServiceRepository;

    public EventServicePersistenceAdapter(SpringDataEventServiceRepository eventServiceRepository) {
        this.eventServiceRepository = eventServiceRepository;
    }

    @Override
    public EventService save(EventService eventService) {
        return eventServiceRepository.save(eventService);
    }

    @Override
    public Optional<EventService> findById(Long id) {
        return eventServiceRepository.findById(id);
    }

    @Override
    public List<EventService> findAll() {
        return eventServiceRepository.findAll();
    }

    @Override
    public List<EventService> findByEventId(Long eventId) {
        return eventServiceRepository.findByEventId(eventId);
    }

    @Override
    public List<EventService> findByOrganizerServiceId(Long organizerServiceId) {
        return eventServiceRepository.findByOrganizerServiceId(organizerServiceId);
    }

    @Override
    public Optional<EventService> findByEventIdAndTrackServiceId(Long eventId, Long trackServiceId) {
        return eventServiceRepository.findByEventIdAndTrackServiceId(eventId, trackServiceId);
    }

    @Override
    public Optional<EventService> findByEventIdAndOrganizerServiceId(Long eventId, Long organizerServiceId) {
        return eventServiceRepository.findByEventIdAndOrganizerServiceId(eventId, organizerServiceId);
    }

    @Override
    public void delete(EventService eventService) {
        eventServiceRepository.delete(eventService);
    }
}
