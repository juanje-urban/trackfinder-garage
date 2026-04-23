package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.EventServiceUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.EventServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventServiceWebMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServiceControllerTest {

    private final EventServiceUseCase eventServiceUseCase = mock(EventServiceUseCase.class);
    private final EventServiceWebMapper eventServiceWebMapper = new EventServiceWebMapper();
    private final EventServiceController eventServiceController =
            new EventServiceController(eventServiceUseCase, eventServiceWebMapper);

    @Test
    void getEventServicesByEventIdMapsResponses() {
        when(eventServiceUseCase.getEventServicesByEventId(5L))
                .thenReturn(List.of(trackEventService(11L, 5L, 7L, "Timing")));

        List<EventServiceResponse> responses = eventServiceController.getEventServicesByEventId(5L);

        assertEquals(1, responses.size());
        assertEquals(11L, responses.get(0).getId());
        assertEquals(7L, responses.get(0).getTrackServiceId());
        assertEquals("Timing", responses.get(0).getTrackServiceName());
        verify(eventServiceUseCase).getEventServicesByEventId(5L);
    }

    private EventService trackEventService(Long id, Long eventId, Long trackServiceId, String serviceName) {
        Event event = new Event();
        event.setId(eventId);

        com.trackfindergarage.backend.domain.model.Service service = new com.trackfindergarage.backend.domain.model.Service();
        service.setId(trackServiceId + 100);
        service.setName(serviceName);

        Track track = new Track();
        track.setId(3L);
        track.setName("Jarama");

        TrackService trackService = new TrackService();
        trackService.setId(trackServiceId);
        trackService.setTrack(track);
        trackService.setService(service);

        EventService eventService = new EventService();
        eventService.setId(id);
        eventService.setEvent(event);
        eventService.setTrackService(trackService);
        eventService.setPrice(new BigDecimal("15.00"));
        return eventService;
    }
}
