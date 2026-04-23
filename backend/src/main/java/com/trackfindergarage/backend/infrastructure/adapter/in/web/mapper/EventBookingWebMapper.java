package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventBookingResponse;
import org.springframework.stereotype.Component;

@Component
public class EventBookingWebMapper {

    public EventBookingResponse toResponse(EventBooking eventBooking) {
        return EventBookingResponse.builder()
                .id(eventBooking.getId())
                .userId(eventBooking.getUser() != null ? eventBooking.getUser().getId() : null)
                .userDisplayName(eventBooking.getUser() != null ? eventBooking.getUser().getDisplayName() : null)
                .eventId(eventBooking.getEvent() != null ? eventBooking.getEvent().getId() : null)
                .eventDate(eventBooking.getEvent() != null ? eventBooking.getEvent().getEventDate() : null)
                .organizerLegalName(eventBooking.getEvent() != null && eventBooking.getEvent().getOrganizer() != null
                        ? eventBooking.getEvent().getOrganizer().getLegalName()
                        : null)
                .trackName(eventBooking.getEvent() != null && eventBooking.getEvent().getTrack() != null
                        ? eventBooking.getEvent().getTrack().getName()
                        : null)
                .bookedAt(eventBooking.getBookedAt())
                .basePriceAtPurchase(eventBooking.getBasePriceAtPurchase())
                .visible(eventBooking.isVisible())
                .build();
    }
}
