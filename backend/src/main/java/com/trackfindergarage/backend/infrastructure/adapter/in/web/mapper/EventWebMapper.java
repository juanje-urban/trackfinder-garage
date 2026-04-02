package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventRequest;
import org.springframework.stereotype.Component;

@Component
public class EventWebMapper {

    public Event toDomain(CreateEventRequest request) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(request.getOrganizerId());

        Track track = new Track();
        track.setId(request.getTrackId());

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(request.getEventDate());
        event.setBasePrice(request.getBasePrice());
        event.setMaxParticipants(request.getMaxParticipants());

        return event;
    }

    public void updateDomain(Event event, UpdateEventRequest request) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(request.getOrganizerId());

        Track track = new Track();
        track.setId(request.getTrackId());

        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(request.getEventDate());
        event.setBasePrice(request.getBasePrice());
        event.setMaxParticipants(request.getMaxParticipants());
    }

    public EventResponse toResponse(Event event, int remainingCapacity) {
        return EventResponse.builder()
                .id(event.getId())
                .organizerId(event.getOrganizer() != null ? event.getOrganizer().getIdUser() : null)
                .organizerLegalName(event.getOrganizer() != null ? event.getOrganizer().getLegalName() : null)
                .trackId(event.getTrack() != null ? event.getTrack().getId() : null)
                .trackName(event.getTrack() != null ? event.getTrack().getName() : null)
                .eventDate(event.getEventDate())
                .basePrice(event.getBasePrice())
                .maxParticipants(event.getMaxParticipants())
                .remainingCapacity(remainingCapacity)
                .build();
    }
}
