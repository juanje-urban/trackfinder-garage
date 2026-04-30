package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventServiceResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper web para convertir servicios de evento en respuestas HTTP.
 *
 * <p>Expone una representación uniforme de extras de evento, ya provengan del catálogo del circuito
 * o del catálogo propio del organizador.</p>
 */
@Component
public class EventServiceWebMapper {

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
}
