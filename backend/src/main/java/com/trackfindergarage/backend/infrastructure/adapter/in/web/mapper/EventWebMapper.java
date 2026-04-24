package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventResponse;
import org.springframework.stereotype.Component;

@Component
public class EventWebMapper {

    public EventResponse toResponse(Event event, int remainingCapacity) {
        return EventResponse.builder()
                .id(event.getId())
                .organizerId(event.getOrganizer() != null ? event.getOrganizer().getIdUser() : null)
                .organizerLegalName(event.getOrganizer() != null ? event.getOrganizer().getLegalName() : null)
                .trackId(event.getTrack() != null ? event.getTrack().getId() : null)
                .trackName(event.getTrack() != null ? event.getTrack().getName() : null)
                .trackShortName(event.getTrack() != null ? event.getTrack().getShortName() : null)
                .eventDate(event.getEventDate())
                .basePrice(event.getBasePrice())
                .maxParticipants(event.getMaxParticipants())
                .remainingCapacity(remainingCapacity)
                .description(event.getDescription())
                .build();
    }
}
