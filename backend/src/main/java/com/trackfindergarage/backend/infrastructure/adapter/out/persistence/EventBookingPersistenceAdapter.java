package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.domain.model.EventBooking;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto de reservas con Spring Data JPA.
 *
 * <p>Delega en el repositorio concreto las búsquedas y escrituras necesarias para la gestión de
 * inscripciones a eventos.</p>
 */
@Component
public class EventBookingPersistenceAdapter implements EventBookingPersistencePort {

    private final SpringDataEventBookingRepository eventBookingRepository;

    public EventBookingPersistenceAdapter(SpringDataEventBookingRepository eventBookingRepository) {
        this.eventBookingRepository = eventBookingRepository;
    }

    @Override
    public EventBooking save(EventBooking eventBooking) {
        return eventBookingRepository.save(eventBooking);
    }

    @Override
    public Optional<EventBooking> findById(Long id) {
        return eventBookingRepository.findById(id);
    }

    @Override
    public List<EventBooking> findByUserId(Long userId) {
        return eventBookingRepository.findByUserId(userId);
    }

    @Override
    public List<EventBooking> findByEventId(Long eventId) {
        return eventBookingRepository.findByEventId(eventId);
    }

    @Override
    public long countByEventId(Long eventId) {
        return eventBookingRepository.countByEventId(eventId);
    }

    @Override
    public Optional<EventBooking> findByUserIdAndEventId(Long userId, Long eventId) {
        return eventBookingRepository.findByUserIdAndEventId(userId, eventId);
    }

    @Override
    public void delete(EventBooking eventBooking) {
        eventBookingRepository.delete(eventBooking);
    }
}
