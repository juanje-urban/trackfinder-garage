package com.trackfindergarage.backend.application.port.in;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;

import java.util.List;
import java.util.Map;

public record OrganizerWorkspaceSnapshot(
        Organizer organizer,
        List<Service> availableServices,
        List<OrganizerService> organizerServices,
        List<Track> tracks,
        List<TrackService> trackServices,
        List<Event> events,
        Map<Long, List<EventService>> eventServicesByEventId,
        OrganizerWorkspaceStatsView stats
) {
}
