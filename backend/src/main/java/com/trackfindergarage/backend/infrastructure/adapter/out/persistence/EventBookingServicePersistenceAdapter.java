package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de persistencia para los servicios extra asociados a reservas.
 *
 * <p>Materializa con JPA las consultas que necesita la aplicación sobre el detalle de servicios
 * contratados en cada reserva.</p>
 */
@Component
public class EventBookingServicePersistenceAdapter implements EventBookingServicePersistencePort {

    private final SpringDataEventBookingServiceRepository eventBookingServiceRepository;

    public EventBookingServicePersistenceAdapter(SpringDataEventBookingServiceRepository eventBookingServiceRepository) {
        this.eventBookingServiceRepository = eventBookingServiceRepository;
    }

    @Override
    public EventBookingService save(EventBookingService eventBookingService) {
        return eventBookingServiceRepository.save(eventBookingService);
    }

    @Override
    public List<EventBookingService> findByEventBookingId(Long eventBookingId) {
        return eventBookingServiceRepository.findByEventBookingId(eventBookingId);
    }

    @Override
    public List<EventBookingService> findByEventBookingEventId(Long eventId) {
        return eventBookingServiceRepository.findByEventBookingEventId(eventId);
    }

    @Override
    public List<EventBookingService> findByEventBookingEventIdAndEventBookingUserId(Long eventId, Long userId) {
        return eventBookingServiceRepository.findByEventBookingEventIdAndEventBookingUserId(eventId, userId);
    }

    @Override
    public List<EventBookingService> findByEventServiceId(Long eventServiceId) {
        return eventBookingServiceRepository.findByEventServiceId(eventServiceId);
    }

    @Override
    public void delete(EventBookingService eventBookingService) {
        eventBookingServiceRepository.delete(eventBookingService);
    }
}
