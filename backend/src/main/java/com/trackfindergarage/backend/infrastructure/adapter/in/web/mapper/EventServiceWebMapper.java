package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateEventServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateEventServiceRequest;
import org.springframework.stereotype.Component;

@Component
public class EventServiceWebMapper {

    public EventService toDomain(CreateEventServiceRequest request) {
        EventService eventService = new EventService();
        populateDomain(eventService, request.getEventId(), request.getTrackServiceId(), request.getOrganizerServiceId(), request.getPrice());
        return eventService;
    }

    public void updateDomain(EventService eventService, UpdateEventServiceRequest request) {
        populateDomain(eventService, request.getEventId(), request.getTrackServiceId(), request.getOrganizerServiceId(), request.getPrice());
    }

    public EventServiceResponse toResponse(EventService eventService) {
        return toResponse(eventService, false);
    }

    public EventServiceResponse toResponse(EventService eventService, boolean hasBookings) {
        return EventServiceResponse.builder()
                .id(eventService.getId())
                .eventId(eventService.getEvent() != null ? eventService.getEvent().getId() : null)
                .trackServiceId(eventService.getTrackService() != null ? eventService.getTrackService().getId() : null)
                .trackServiceName(
                        eventService.getTrackService() != null && eventService.getTrackService().getService() != null
                                ? eventService.getTrackService().getService().getName()
                                : null
                )
                .organizerServiceId(
                        eventService.getOrganizerService() != null ? eventService.getOrganizerService().getId() : null
                )
                .organizerServiceName(
                        eventService.getOrganizerService() != null && eventService.getOrganizerService().getService() != null
                                ? eventService.getOrganizerService().getService().getName()
                                : null
                )
                .price(eventService.getPrice())
                .hasBookings(hasBookings)
                .build();
    }

    private void populateDomain(EventService eventService,
                                Long eventId,
                                Long trackServiceId,
                                Long organizerServiceId,
                                java.math.BigDecimal price) {
        Event event = new Event();
        event.setId(eventId);
        eventService.setEvent(event);

        if (trackServiceId != null) {
            TrackService trackService = new TrackService();
            trackService.setId(trackServiceId);
            eventService.setTrackService(trackService);
        } else {
            eventService.setTrackService(null);
        }

        if (organizerServiceId != null) {
            OrganizerService organizerService = new OrganizerService();
            organizerService.setId(organizerServiceId);
            eventService.setOrganizerService(organizerService);
        } else {
            eventService.setOrganizerService(null);
        }

        eventService.setPrice(price);
    }
}
