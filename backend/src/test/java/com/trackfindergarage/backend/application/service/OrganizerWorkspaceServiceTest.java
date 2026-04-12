package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventServiceUseCase;
import com.trackfindergarage.backend.application.port.in.EventUseCase;
import com.trackfindergarage.backend.application.port.in.OrganizerEventDraft;
import com.trackfindergarage.backend.application.port.in.OrganizerEventServiceDraft;
import com.trackfindergarage.backend.application.port.in.OrganizerServiceUseCase;
import com.trackfindergarage.backend.application.port.in.ServiceUseCase;
import com.trackfindergarage.backend.application.port.in.TrackServiceUseCase;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ConflictException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerWorkspaceServiceTest {

    private static final Long ORGANIZER_ID = 7L;
    private static final String ORGANIZER_EMAIL = "organizer@example.com";

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private OrganizerPersistencePort organizerPersistencePort;

    @Mock
    private EventServicePersistencePort eventServicePersistencePort;

    @Mock
    private EventBookingPersistencePort eventBookingPersistencePort;

    @Mock
    private EventBookingServicePersistencePort eventBookingServicePersistencePort;

    @Mock
    private TrackPersistencePort trackPersistencePort;

    @Mock
    private ServiceUseCase serviceUseCase;

    @Mock
    private TrackServiceUseCase trackServiceUseCase;

    @Mock
    private OrganizerServiceUseCase organizerServiceUseCase;

    @Mock
    private EventUseCase eventUseCase;

    @Mock
    private EventServiceUseCase eventServiceUseCase;

    @InjectMocks
    private OrganizerWorkspaceService organizerWorkspaceService;

    @Test
    void getWorkspaceBuildsAggregatedStatsAndFiltersDisabledServices() {
        Organizer organizer = stubAuthenticatedOrganizer(true);

        Service enabledAlpha = serviceWithId(1L, "Alpha coaching", true);
        Service disabledService = serviceWithId(2L, "Disabled wash", false);
        Service enabledZulu = serviceWithId(3L, "Zulu photos", true);
        when(serviceUseCase.getAllServicesAllowedForOrganizer())
                .thenReturn(List.of(enabledZulu, disabledService, enabledAlpha));

        when(organizerServiceUseCase.getOrganizerServicesByOrganizerId(ORGANIZER_ID))
                .thenReturn(List.of(
                        organizerServiceWithId(22L, organizer, serviceWithId(5L, "Video", true)),
                        organizerServiceWithId(21L, organizer, serviceWithId(4L, "Briefing", true))
                ));

        when(trackPersistencePort.findAll())
                .thenReturn(List.of(trackWithId(2L, "Jarama"), trackWithId(1L, "Calafat")));

        when(trackServiceUseCase.getAllTrackServices())
                .thenReturn(List.of(
                        trackServiceWithId(32L, trackWithId(2L, "Jarama"), serviceWithId(6L, "Fuel", true)),
                        trackServiceWithId(31L, trackWithId(1L, "Calafat"), serviceWithId(7L, "Timing", true))
                ));

        Event futureEvent = eventWithId(101L, LocalDate.now().plusDays(20), "Jarama", new BigDecimal("120.00"), 20, organizer);
        Event pastEvent = eventWithId(102L, LocalDate.now().minusDays(5), "Calafat", new BigDecimal("90.00"), 10, organizer);
        when(eventUseCase.getEventsByOrganizerId(ORGANIZER_ID)).thenReturn(List.of(futureEvent, pastEvent));

        when(eventServiceUseCase.getEventServicesByEventId(101L))
                .thenReturn(List.of(eventServiceWithTrackService(61L, futureEvent, 32L, "Fuel", new BigDecimal("15.00"))));
        when(eventServiceUseCase.getEventServicesByEventId(102L))
                .thenReturn(List.of(eventServiceWithOrganizerService(62L, pastEvent, 21L, "Briefing", new BigDecimal("20.00"))));

        when(eventBookingPersistencePort.findByEventId(101L))
                .thenReturn(List.of(
                        bookingWithBasePrice(201L, futureEvent, new BigDecimal("120.00")),
                        bookingWithBasePrice(202L, futureEvent, new BigDecimal("120.00"))
                ));
        when(eventBookingPersistencePort.findByEventId(102L))
                .thenReturn(List.of(bookingWithBasePrice(203L, pastEvent, new BigDecimal("90.00"))));

        when(eventBookingServicePersistencePort.findByEventBookingEventId(101L))
                .thenReturn(List.of(
                        soldServiceWithPrice(new BigDecimal("15.00")),
                        soldServiceWithPrice(new BigDecimal("15.00"))
                ));
        when(eventBookingServicePersistencePort.findByEventBookingEventId(102L))
                .thenReturn(List.of(soldServiceWithPrice(new BigDecimal("20.00"))));

        var snapshot = organizerWorkspaceService.getWorkspace(ORGANIZER_EMAIL);

        assertEquals(List.of("Alpha coaching", "Zulu photos"),
                snapshot.availableServices().stream().map(Service::getName).toList());
        assertEquals(List.of("Calafat", "Jarama"),
                snapshot.events().stream().map(event -> event.getTrack().getName()).toList());
        assertEquals(new BigDecimal("330.00"), snapshot.stats().totalBaseRevenue());
        assertEquals(new BigDecimal("50.00"), snapshot.stats().totalServiceRevenue());
        assertEquals(new BigDecimal("380.00"), snapshot.stats().totalGrossRevenue());
        assertEquals(3L, snapshot.stats().totalBookings());
        assertEquals(3L, snapshot.stats().totalSoldServices());
        assertEquals(1L, snapshot.stats().futureEvents());
        assertEquals(1L, snapshot.stats().pastEvents());
        assertEquals(30, snapshot.stats().totalCapacity());
        assertEquals(27, snapshot.stats().totalRemainingCapacity());
        assertEquals(List.of("Calafat", "Jarama"),
                snapshot.stats().eventStats().stream().map(stat -> stat.trackName()).toList());
    }

    @Test
    void removeOrganizerServiceThrowsWhenAttachedToFutureEvent() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        com.trackfindergarage.backend.domain.model.OrganizerService organizerService =
                organizerServiceWithId(15L, organizer, serviceWithId(1L, "Instructor", true));

        when(organizerServiceUseCase.getOrganizerServiceById(15L)).thenReturn(organizerService);
        when(eventServicePersistencePort.findByOrganizerServiceId(15L))
                .thenReturn(List.of(eventServiceWithOrganizerService(
                        80L,
                        eventWithId(300L, LocalDate.now().plusDays(10), "Jarama", new BigDecimal("100.00"), 20, organizer),
                        15L,
                        "Instructor",
                        new BigDecimal("35.00")
                )));

        assertThrows(ConflictException.class,
                () -> organizerWorkspaceService.removeOrganizerService(ORGANIZER_EMAIL, 15L));

        verify(organizerServiceUseCase, never()).deleteOrganizerService(15L);
    }

    @Test
    void removeOrganizerServiceDisablesCatalogAssignmentWhenOnlyPastEventsReferenceIt() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        com.trackfindergarage.backend.domain.model.OrganizerService organizerService =
                organizerServiceWithId(17L, organizer, serviceWithId(3L, "Catering", true));

        when(organizerServiceUseCase.getOrganizerServiceById(17L)).thenReturn(organizerService);
        when(eventServicePersistencePort.findByOrganizerServiceId(17L))
                .thenReturn(List.of(eventServiceWithOrganizerService(
                        81L,
                        eventWithId(301L, LocalDate.now().minusDays(10), "Jarama", new BigDecimal("100.00"), 20, organizer),
                        17L,
                        "Catering",
                        new BigDecimal("20.00")
                )));
        stubWorkspaceSnapshotDependencies();

        organizerWorkspaceService.removeOrganizerService(ORGANIZER_EMAIL, 17L);

        verify(organizerServiceUseCase).deleteOrganizerService(17L);
    }

    @Test
    void removeOrganizerServiceDeletesAssignmentWhenItIsNotUsedByFutureEvents() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        com.trackfindergarage.backend.domain.model.OrganizerService organizerService =
                organizerServiceWithId(16L, organizer, serviceWithId(2L, "Photo pack", true));

        when(organizerServiceUseCase.getOrganizerServiceById(16L)).thenReturn(organizerService);
        when(eventServicePersistencePort.findByOrganizerServiceId(16L)).thenReturn(List.of());
        stubWorkspaceSnapshotDependencies();

        organizerWorkspaceService.removeOrganizerService(ORGANIZER_EMAIL, 16L);

        verify(organizerServiceUseCase).deleteOrganizerService(16L);
    }

    @Test
    void createEventCreatesMissingServicesAndUpdatesExistingPrices() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        Event createdEvent = eventWithId(501L, LocalDate.now().plusDays(30), "Jarama", new BigDecimal("140.00"), 24, organizer);
        EventService existingEventService =
                eventServiceWithTrackService(71L, createdEvent, 41L, "Timing", new BigDecimal("18.00"));

        when(eventUseCase.createEvent(any(Event.class))).thenReturn(createdEvent);
        when(eventServiceUseCase.getEventServicesByEventId(501L)).thenReturn(List.of(existingEventService));
        stubWorkspaceSnapshotDependencies();

        OrganizerEventDraft draft = new OrganizerEventDraft(
                9L,
                LocalDate.now().plusDays(30),
                new BigDecimal("140.00"),
                24,
                "Track day",
                List.of(
                        new OrganizerEventServiceDraft(41L, null, new BigDecimal("20.00")),
                        new OrganizerEventServiceDraft(null, 55L, new BigDecimal("35.00"))
                )
        );

        organizerWorkspaceService.createEvent(ORGANIZER_EMAIL, draft);

        verify(eventServiceUseCase).updateEventService(
                argThat(id -> id.equals(71L)),
                argThat(eventService ->
                        eventService.getTrackService() != null
                                && eventService.getTrackService().getId().equals(41L)
                                && eventService.getOrganizerService() == null
                                && eventService.getPrice().compareTo(new BigDecimal("20.00")) == 0
                )
        );
        verify(eventServiceUseCase).createEventService(argThat(eventService ->
                eventService.getOrganizerService() != null
                        && eventService.getOrganizerService().getId().equals(55L)
                        && eventService.getTrackService() == null
                        && eventService.getPrice().compareTo(new BigDecimal("35.00")) == 0
        ));
    }

    @Test
    void updateEventThrowsWhenTryingToRemoveServiceThatAlreadyHasBookings() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        Event existingEvent = eventWithId(601L, LocalDate.now().plusDays(25), "Jarama", new BigDecimal("150.00"), 20, organizer);
        EventService existingService =
                eventServiceWithOrganizerService(91L, existingEvent, 88L, "Instructor", new BigDecimal("40.00"));

        when(eventUseCase.getEventById(601L)).thenReturn(existingEvent);
        when(eventUseCase.updateEvent(any(Long.class), any(Event.class))).thenReturn(existingEvent);
        when(eventServiceUseCase.getEventServicesByEventId(601L)).thenReturn(List.of(existingService));
        when(eventBookingServicePersistencePort.findByEventServiceId(91L))
                .thenReturn(List.of(soldServiceWithPrice(new BigDecimal("40.00"))));

        OrganizerEventDraft draft = new OrganizerEventDraft(
                existingEvent.getTrack().getId(),
                LocalDate.now().plusDays(25),
                new BigDecimal("150.00"),
                20,
                "Updated event",
                List.of()
        );

        assertThrows(ConflictException.class,
                () -> organizerWorkspaceService.updateEvent(ORGANIZER_EMAIL, 601L, draft));

        verify(eventServiceUseCase, never()).deleteEventService(91L);
    }

    @Test
    void updateEventThrowsWhenTryingToChangeTrack() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        Event existingEvent = eventWithId(602L, LocalDate.now().plusDays(25), "Jarama", new BigDecimal("150.00"), 20, organizer);

        when(eventUseCase.getEventById(602L)).thenReturn(existingEvent);

        OrganizerEventDraft draft = new OrganizerEventDraft(
                existingEvent.getTrack().getId() + 1,
                existingEvent.getEventDate(),
                new BigDecimal("150.00"),
                20,
                "Updated event",
                List.of()
        );

        assertThrows(ConflictException.class,
                () -> organizerWorkspaceService.updateEvent(ORGANIZER_EMAIL, 602L, draft));

        verify(eventUseCase, never()).updateEvent(any(Long.class), any(Event.class));
    }

    @Test
    void updateEventThrowsWhenTryingToChangeDate() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        Event existingEvent = eventWithId(603L, LocalDate.now().plusDays(25), "Jarama", new BigDecimal("150.00"), 20, organizer);

        when(eventUseCase.getEventById(603L)).thenReturn(existingEvent);

        OrganizerEventDraft draft = new OrganizerEventDraft(
                existingEvent.getTrack().getId(),
                existingEvent.getEventDate().plusDays(1),
                new BigDecimal("150.00"),
                20,
                "Updated event",
                List.of()
        );

        assertThrows(ConflictException.class,
                () -> organizerWorkspaceService.updateEvent(ORGANIZER_EMAIL, 603L, draft));

        verify(eventUseCase, never()).updateEvent(any(Long.class), any(Event.class));
    }

    @Test
    void deleteEventThrowsWhenEventIsNotFuture() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        Event pastEvent = eventWithId(701L, LocalDate.now().minusDays(1), "Jarama", new BigDecimal("150.00"), 20, organizer);

        when(eventUseCase.getEventById(701L)).thenReturn(pastEvent);

        assertThrows(IllegalArgumentException.class,
                () -> organizerWorkspaceService.deleteEvent(ORGANIZER_EMAIL, 701L));

        verify(eventUseCase, never()).deleteEvent(701L);
    }

    @Test
    void deleteEventThrowsWhenEventAlreadyHasBookings() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        Event futureEvent = eventWithId(702L, LocalDate.now().plusDays(20), "Jarama", new BigDecimal("150.00"), 20, organizer);

        when(eventUseCase.getEventById(702L)).thenReturn(futureEvent);
        when(eventBookingPersistencePort.countByEventId(702L)).thenReturn(1L);

        assertThrows(ConflictException.class,
                () -> organizerWorkspaceService.deleteEvent(ORGANIZER_EMAIL, 702L));

        verify(eventUseCase, never()).deleteEvent(702L);
    }

    @Test
    void deleteEventRemovesServicesAndEventWhenThereAreNoBookings() {
        Organizer organizer = stubAuthenticatedOrganizer(true);
        Event futureEvent = eventWithId(703L, LocalDate.now().plusDays(20), "Jarama", new BigDecimal("150.00"), 20, organizer);

        when(eventUseCase.getEventById(703L)).thenReturn(futureEvent);
        when(eventBookingPersistencePort.countByEventId(703L)).thenReturn(0L);
        when(eventServiceUseCase.getEventServicesByEventId(703L)).thenReturn(List.of(
                eventServiceWithTrackService(111L, futureEvent, 41L, "Timing", new BigDecimal("18.00")),
                eventServiceWithOrganizerService(112L, futureEvent, 88L, "Instructor", new BigDecimal("40.00"))
        ));
        stubWorkspaceSnapshotDependencies();

        organizerWorkspaceService.deleteEvent(ORGANIZER_EMAIL, 703L);

        verify(eventServiceUseCase).deleteEventService(111L);
        verify(eventServiceUseCase).deleteEventService(112L);
        verify(eventUseCase).deleteEvent(703L);
    }

    private Organizer stubAuthenticatedOrganizer(boolean enabled) {
        User user = userWithOrganizerRole();
        Organizer organizer = organizerWithUser(user, enabled);

        when(userPersistencePort.findByEmail(ORGANIZER_EMAIL)).thenReturn(Optional.of(user));
        when(organizerPersistencePort.findById(ORGANIZER_ID)).thenReturn(Optional.of(organizer));

        return organizer;
    }

    @Test
    void getWorkspaceThrowsWhenOrganizerIsPendingApproval() {
        stubAuthenticatedOrganizer(false);

        assertThrows(AccessDeniedException.class,
                () -> organizerWorkspaceService.getWorkspace(ORGANIZER_EMAIL));
    }

    private void stubWorkspaceSnapshotDependencies() {
        when(serviceUseCase.getAllServicesAllowedForOrganizer()).thenReturn(List.of());
        when(organizerServiceUseCase.getOrganizerServicesByOrganizerId(ORGANIZER_ID)).thenReturn(List.of());
        when(trackPersistencePort.findAll()).thenReturn(List.of());
        when(trackServiceUseCase.getAllTrackServices()).thenReturn(List.of());
        when(eventUseCase.getEventsByOrganizerId(ORGANIZER_ID)).thenReturn(List.of());
    }

    private User userWithOrganizerRole() {
        Role role = new Role();
        role.setRoleName("ORGANIZER");

        User user = new User();
        user.setId(ORGANIZER_ID);
        user.setEmail(ORGANIZER_EMAIL);
        user.setDisplayName("organizer");
        user.setRole(role);
        return user;
    }

    private Organizer organizerWithUser(User user, boolean enabled) {
        Organizer organizer = new Organizer();
        organizer.setIdUser(user.getId());
        organizer.setUser(user);
        organizer.setEnabled(enabled);
        organizer.setLegalName("Organizer demo");
        return organizer;
    }

    private Service serviceWithId(Long id, String name, boolean enabled) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        service.setEnabled(enabled);
        return service;
    }

    private Track trackWithId(Long id, String name) {
        Track track = new Track();
        track.setId(id);
        track.setName(name);
        track.setLocation("Spain");
        track.setDescription("Circuit description");
        return track;
    }

    private com.trackfindergarage.backend.domain.model.OrganizerService organizerServiceWithId(Long id,
                                                                                              Organizer organizer,
                                                                                              Service service) {
        com.trackfindergarage.backend.domain.model.OrganizerService organizerService =
                new com.trackfindergarage.backend.domain.model.OrganizerService();
        organizerService.setId(id);
        organizerService.setOrganizer(organizer);
        organizerService.setService(service);
        return organizerService;
    }

    private TrackService trackServiceWithId(Long id, Track track, Service service) {
        TrackService trackService = new TrackService();
        trackService.setId(id);
        trackService.setTrack(track);
        trackService.setService(service);
        return trackService;
    }

    private Event eventWithId(Long id,
                              LocalDate eventDate,
                              String trackName,
                              BigDecimal basePrice,
                              int maxParticipants,
                              Organizer organizer) {
        Event event = new Event();
        event.setId(id);
        event.setEventDate(eventDate);
        event.setBasePrice(basePrice);
        event.setMaxParticipants(maxParticipants);
        event.setOrganizer(organizer);
        event.setTrack(trackWithId(id + 1000, trackName));
        event.setDescription("Event description");
        return event;
    }

    private EventService eventServiceWithTrackService(Long id,
                                                      Event event,
                                                      Long trackServiceId,
                                                      String serviceName,
                                                      BigDecimal price) {
        EventService eventService = new EventService();
        eventService.setId(id);
        eventService.setEvent(event);
        eventService.setTrackService(trackServiceWithId(
                trackServiceId,
                event.getTrack(),
                serviceWithId(trackServiceId + 100, serviceName, true)
        ));
        eventService.setPrice(price);
        return eventService;
    }

    private EventService eventServiceWithOrganizerService(Long id,
                                                          Event event,
                                                          Long organizerServiceId,
                                                          String serviceName,
                                                          BigDecimal price) {
        EventService eventService = new EventService();
        eventService.setId(id);
        eventService.setEvent(event);
        eventService.setOrganizerService(organizerServiceWithId(
                organizerServiceId,
                event.getOrganizer(),
                serviceWithId(organizerServiceId + 100, serviceName, true)
        ));
        eventService.setPrice(price);
        return eventService;
    }

    private EventBooking bookingWithBasePrice(Long id, Event event, BigDecimal basePriceAtPurchase) {
        EventBooking booking = new EventBooking();
        booking.setId(id);
        booking.setEvent(event);
        booking.setBasePriceAtPurchase(basePriceAtPurchase);
        booking.setVisible(true);
        return booking;
    }

    private EventBookingService soldServiceWithPrice(BigDecimal priceAtPurchase) {
        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setPriceAtPurchase(priceAtPurchase);
        return eventBookingService;
    }
}
