package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEventServiceRepository extends JpaRepository<EventService, Long> {

    List<EventService> findByEventId(Long eventId);

    Optional<EventService> findByEventIdAndTrackServiceId(Long eventId, Long trackServiceId);

    Optional<EventService> findByEventIdAndOrganizerServiceId(Long eventId, Long organizerServiceId);
}
