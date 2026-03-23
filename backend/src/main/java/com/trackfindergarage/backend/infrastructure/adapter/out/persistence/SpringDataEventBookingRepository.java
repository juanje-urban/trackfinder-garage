package com.trackfindergarage.backend.infrastructure.adapter.out.persistence;

import com.trackfindergarage.backend.domain.model.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEventBookingRepository extends JpaRepository<EventBooking, Long> {

    List<EventBooking> findByUserId(Long userId);

    List<EventBooking> findByEventId(Long eventId);

    Optional<EventBooking> findByUserIdAndEventId(Long userId, Long eventId);
}
