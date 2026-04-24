package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataEventRepository extends JpaRepository<Event, Long> {

    @Override
    @EntityGraph(attributePaths = {"organizer", "track"})
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {"organizer", "track"})
    List<Event> findByOrganizerIdUser(Long organizerId);

    @EntityGraph(attributePaths = {"organizer", "track"})
    Optional<Event> findByTrackIdAndEventDate(Long trackId, LocalDate eventDate);

    @EntityGraph(attributePaths = {"organizer", "track"})
    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate fromDate);
}
