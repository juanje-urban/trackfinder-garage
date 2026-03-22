package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataEventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizerIdUser(Long organizerId);

    List<Event> findByTrackId(Long trackId);

    List<Event> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<Event> findByTrackIdAndEventDate(Long trackId, LocalDate eventDate);
}
