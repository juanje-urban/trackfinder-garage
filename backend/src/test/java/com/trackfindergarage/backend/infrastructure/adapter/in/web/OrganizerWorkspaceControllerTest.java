package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.OrganizerEventDraft;
import com.trackfindergarage.backend.application.port.in.OrganizerEventServiceDraft;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceEventStatsView;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceSnapshot;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceStatsView;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceUseCase;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerCatalogServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerEventServiceInput;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerWorkspaceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpsertOrganizerEventRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventServiceWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.EventWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerServiceWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.OrganizerWorkspaceWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.ServiceWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackServiceWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizerWorkspaceControllerTest {

    private final OrganizerWorkspaceUseCase organizerWorkspaceUseCase = mock(OrganizerWorkspaceUseCase.class);
    private final OrganizerWorkspaceWebMapper organizerWorkspaceWebMapper = new OrganizerWorkspaceWebMapper(
            new OrganizerWebMapper(),
            new ServiceWebMapper(),
            new OrganizerServiceWebMapper(),
            new TrackWebMapper(),
            new TrackServiceWebMapper(),
            new EventWebMapper(),
            new EventServiceWebMapper()
    );
    private final OrganizerWorkspaceController organizerWorkspaceController =
            new OrganizerWorkspaceController(organizerWorkspaceUseCase, organizerWorkspaceWebMapper);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void getWorkspaceMapsNestedResponse() {
        when(authentication.getName()).thenReturn("organizer@example.com");
        when(organizerWorkspaceUseCase.getWorkspace("organizer@example.com")).thenReturn(snapshot());

        OrganizerWorkspaceResponse response = organizerWorkspaceController.getWorkspace(authentication);

        assertEquals("tracklimits", response.getOrganizer().getDisplayName());
        assertEquals(1, response.getAvailableServices().size());
        assertEquals(1, response.getEvents().size());
        assertEquals(true, response.getEvents().get(0).getServices().get(0).getHasBookings());
    }

    @Test
    void organizerServiceEndpointsDelegateToUseCase() {
        CreateOrganizerCatalogServiceRequest request = new CreateOrganizerCatalogServiceRequest();
        request.setServiceId(4L);

        when(authentication.getName()).thenReturn("organizer@example.com");
        when(organizerWorkspaceUseCase.addOrganizerService("organizer@example.com", 4L)).thenReturn(snapshot());
        when(organizerWorkspaceUseCase.removeOrganizerService("organizer@example.com", 9L)).thenReturn(snapshot());

        OrganizerWorkspaceResponse added = organizerWorkspaceController.addOrganizerService(request, authentication);
        OrganizerWorkspaceResponse removed = organizerWorkspaceController.removeOrganizerService(9L, authentication);

        assertEquals("Track days", added.getAvailableServices().get(0).getName());
        assertEquals("Track days", removed.getAvailableServices().get(0).getName());
        verify(organizerWorkspaceUseCase).addOrganizerService("organizer@example.com", 4L);
        verify(organizerWorkspaceUseCase).removeOrganizerService("organizer@example.com", 9L);
    }

    @Test
    void eventEndpointsDelegateWithMappedDraft() {
        UpsertOrganizerEventRequest request = new UpsertOrganizerEventRequest();
        request.setTrackId(7L);
        request.setEventDate(LocalDate.of(2026, 6, 10));
        request.setBasePrice(new BigDecimal("120.00"));
        request.setMaxParticipants(40);
        request.setDescription("Track day");

        OrganizerEventServiceInput serviceInput = new OrganizerEventServiceInput();
        serviceInput.setTrackServiceId(21L);
        serviceInput.setOrganizerServiceId(null);
        serviceInput.setPrice(new BigDecimal("15.00"));
        request.setServices(List.of(serviceInput));

        when(authentication.getName()).thenReturn("organizer@example.com");
        when(organizerWorkspaceUseCase.createEvent(any(String.class), any(OrganizerEventDraft.class))).thenReturn(snapshot());
        when(organizerWorkspaceUseCase.updateEvent(any(String.class), any(Long.class), any(OrganizerEventDraft.class))).thenReturn(snapshot());
        when(organizerWorkspaceUseCase.deleteEvent("organizer@example.com", 5L)).thenReturn(snapshot());

        OrganizerWorkspaceResponse created = organizerWorkspaceController.createEvent(request, authentication);
        OrganizerWorkspaceResponse updated = organizerWorkspaceController.updateEvent(5L, request, authentication);
        OrganizerWorkspaceResponse deleted = organizerWorkspaceController.deleteEvent(5L, authentication);

        assertEquals(1, created.getEvents().size());
        assertEquals(1, updated.getEvents().size());
        assertEquals(1, deleted.getEvents().size());

        verify(organizerWorkspaceUseCase).createEvent(
                argThat(email -> email.equals("organizer@example.com")),
                argThat(draft -> draft.trackId().equals(7L)
                        && draft.eventDate().equals(LocalDate.of(2026, 6, 10))
                        && draft.basePrice().compareTo(new BigDecimal("120.00")) == 0
                        && draft.maxParticipants().equals(40)
                        && draft.description().equals("Track day")
                        && draft.services().equals(List.of(new OrganizerEventServiceDraft(21L, null, new BigDecimal("15.00")))))
        );
        verify(organizerWorkspaceUseCase).updateEvent(
                argThat(email -> email.equals("organizer@example.com")),
                argThat(id -> id.equals(5L)),
                argThat(draft -> draft.trackId().equals(7L)
                        && draft.services().size() == 1
                        && draft.services().get(0).trackServiceId().equals(21L))
        );
        verify(organizerWorkspaceUseCase).deleteEvent("organizer@example.com", 5L);
    }

    private OrganizerWorkspaceSnapshot snapshot() {
        Role role = new Role();
        role.setId(2L);
        role.setRoleName("ORGANIZER");

        User user = new User();
        user.setId(5L);
        user.setDisplayName("tracklimits");
        user.setEmail("organizer@example.com");
        user.setEnabled(true);
        user.setRole(role);

        Organizer organizer = new Organizer();
        organizer.setIdUser(5L);
        organizer.setUser(user);
        organizer.setLegalName("Track Limits");
        organizer.setCif("B12345678");
        organizer.setEnabled(true);

        Service availableService = new Service();
        availableService.setId(4L);
        availableService.setName("Track days");
        availableService.setDescription("Desc");
        availableService.setAllowedForOrganizer(true);
        availableService.setAllowedForTrack(false);
        availableService.setEnabled(true);

        OrganizerService organizerService = new OrganizerService();
        organizerService.setId(9L);
        organizerService.setOrganizer(organizer);
        organizerService.setService(availableService);
        organizerService.setEnabled(true);

        Track track = new Track();
        track.setId(7L);
        track.setName("Jarama");
        track.setShortName("jarama");
        track.setLocation("Madrid");
        track.setDescription("Legendario");

        TrackService trackService = new TrackService();
        trackService.setId(21L);
        trackService.setTrack(track);
        trackService.setService(availableService);

        Event event = new Event();
        event.setId(5L);
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(LocalDate.of(2026, 6, 10));
        event.setBasePrice(new BigDecimal("120.00"));
        event.setMaxParticipants(40);
        event.setDescription("Track day");

        EventService eventService = new EventService();
        eventService.setId(31L);
        eventService.setEvent(event);
        eventService.setTrackService(trackService);
        eventService.setPrice(new BigDecimal("15.00"));

        OrganizerWorkspaceEventStatsView eventStats = new OrganizerWorkspaceEventStatsView(
                5L,
                "Jarama",
                LocalDate.of(2026, 6, 10),
                12,
                8,
                28,
                40,
                new BigDecimal("1440.00"),
                new BigDecimal("120.00"),
                new BigDecimal("1560.00")
        );

        OrganizerWorkspaceStatsView stats = new OrganizerWorkspaceStatsView(
                new BigDecimal("1440.00"),
                new BigDecimal("120.00"),
                new BigDecimal("1560.00"),
                12,
                8,
                1,
                0,
                40,
                28,
                List.of(eventStats)
        );

        return new OrganizerWorkspaceSnapshot(
                organizer,
                List.of(availableService),
                List.of(organizerService),
                List.of(track),
                List.of(trackService),
                List.of(event),
                Map.of(5L, List.of(eventService)),
                Map.of(5L, Set.of(31L)),
                stats
        );
    }
}
