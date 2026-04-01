package com.trackfindergarage.backend.infrastructure.config;

import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataEventBookingRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataEventBookingServiceRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataEventRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataEventServiceRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataLapTimeRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataMessageRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataOrganizerRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataOrganizerServiceRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataRoleRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataServiceRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataTrackRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataTrackServiceRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    private static final String UNREAD_MESSAGE_CONTENT =
            "Hola, me interesa una tanda en Jarama para abril. \u00BFTen\u00E9is previsto organizar alguna? Gracias.";

    @Mock
    private SpringDataRoleRepository roleRepository;

    @Mock
    private SpringDataUserRepository userRepository;

    @Mock
    private SpringDataEventRepository eventRepository;

    @Mock
    private SpringDataEventBookingRepository eventBookingRepository;

    @Mock
    private SpringDataEventServiceRepository eventServiceRepository;

    @Mock
    private SpringDataEventBookingServiceRepository eventBookingServiceRepository;

    @Mock
    private SpringDataLapTimeRepository lapTimeRepository;

    @Mock
    private SpringDataMessageRepository messageRepository;

    @Mock
    private SpringDataOrganizerRepository organizerRepository;

    @Mock
    private SpringDataOrganizerServiceRepository organizerServiceRepository;

    @Mock
    private SpringDataTrackRepository trackRepository;

    @Mock
    private SpringDataTrackServiceRepository trackServiceRepository;

    @Mock
    private SpringDataServiceRepository serviceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DemoDataSeeder demoDataSeeder;

    @BeforeEach
    void setUp() {
        demoDataSeeder = new DemoDataSeeder(
                roleRepository,
                userRepository,
                eventRepository,
                eventBookingRepository,
                eventServiceRepository,
                eventBookingServiceRepository,
                lapTimeRepository,
                messageRepository,
                organizerRepository,
                organizerServiceRepository,
                trackRepository,
                trackServiceRepository,
                serviceRepository,
                passwordEncoder
        );
    }

    @Test
    void runSeedsAllDemoDataWhenRepositoriesAreEmpty() {
        SeedState state = new SeedState();
        stubCoreRepositories(state);
        stubNestedSeedLookups(state);
        stubInitialCreationSaves(state);
        stubRemainingSeedingSaves(state);

        demoDataSeeder.run();

        assertSeedCreationCounts();
        assertSavedEventsHaveExpectedDateDistribution();
        assertSavedBookingsBelongToStandardUsers();
        assertSavedEventServicesHavePositivePrice();
        assertSavedBookingServicesMirrorEventServicePricing();
        assertSavedLapTimesContainExpectedVehicle();
        assertUnreadMessageWasSeeded();
    }

    @Test
    void runSkipsIdempotentDemoResourcesWhenTheyAlreadyExist() {
        SeedState state = new SeedState();
        stubCoreRepositories(state);
        stubNestedSeedLookups(state);
        populateExistingDemoResources(state);

        demoDataSeeder.run();

        verify(roleRepository, never()).save(any(Role.class));
        verify(trackRepository, never()).save(any(Track.class));
        verify(serviceRepository, never()).save(any(Service.class));
        verify(organizerServiceRepository, never()).save(any(OrganizerService.class));
        verify(trackServiceRepository, never()).save(any(TrackService.class));
        verify(eventRepository, never()).save(any(Event.class));
        verify(eventServiceRepository, never()).save(any(EventService.class));
        verify(eventBookingRepository, never()).save(any(EventBooking.class));
        verify(eventBookingServiceRepository, never()).save(any(EventBookingService.class));
        verify(lapTimeRepository, never()).save(any(LapTime.class));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void runThrowsWhenOrganizerCannotBeRecoveredForDemoServices() {
        SeedState state = new SeedState();
        stubCoreRepositories(state);
        stubInitialCreationSaves(state);

        when(organizerRepository.findByLegalName("TrackEvents S.L.")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> demoDataSeeder.run());

        assertEquals("Organizer not found in demo seed: TrackEvents S.L.", exception.getMessage());
    }

    private void assertSeedCreationCounts() {
        verify(roleRepository, times(3)).save(any(Role.class));
        verify(userRepository, times(9)).save(any(User.class));
        verify(organizerRepository, times(3)).save(any(Organizer.class));
        verify(trackRepository, times(6)).save(any(Track.class));
        verify(serviceRepository, times(12)).save(any(Service.class));
        verify(organizerServiceRepository, times(17)).save(any(OrganizerService.class));
        verify(trackServiceRepository, times(23)).save(any(TrackService.class));
        verify(eventRepository, times(8)).save(any(Event.class));
        verify(eventServiceRepository, times(46)).save(any(EventService.class));
        verify(eventBookingRepository, times(20)).save(any(EventBooking.class));
        verify(eventBookingServiceRepository, times(34)).save(any(EventBookingService.class));
        verify(lapTimeRepository, times(10)).save(any(LapTime.class));
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    private void assertSavedEventsHaveExpectedDateDistribution() {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(8)).save(eventCaptor.capture());

        assertEquals(4, eventCaptor.getAllValues().stream()
                .filter(event -> event.getEventDate().isBefore(LocalDate.now()))
                .count());
        assertEquals(4, eventCaptor.getAllValues().stream()
                .filter(event -> event.getEventDate().isAfter(LocalDate.now()))
                .count());
    }

    private void assertSavedBookingsBelongToStandardUsers() {
        ArgumentCaptor<EventBooking> eventBookingCaptor = ArgumentCaptor.forClass(EventBooking.class);
        verify(eventBookingRepository, times(20)).save(eventBookingCaptor.capture());

        assertTrue(eventBookingCaptor.getAllValues().stream()
                .allMatch(eventBooking -> "USER".equals(eventBooking.getUser().getRole().getRoleName())));
    }

    private void assertSavedEventServicesHavePositivePrice() {
        ArgumentCaptor<EventService> eventServiceCaptor = ArgumentCaptor.forClass(EventService.class);
        verify(eventServiceRepository, times(46)).save(eventServiceCaptor.capture());

        assertTrue(eventServiceCaptor.getAllValues().stream()
                .allMatch(eventService -> eventService.getPrice().compareTo(BigDecimal.ZERO) > 0));
    }

    private void assertSavedBookingServicesMirrorEventServicePricing() {
        ArgumentCaptor<EventBookingService> eventBookingServiceCaptor = ArgumentCaptor.forClass(EventBookingService.class);
        verify(eventBookingServiceRepository, times(34)).save(eventBookingServiceCaptor.capture());

        assertTrue(eventBookingServiceCaptor.getAllValues().stream()
                .allMatch(eventBookingService -> eventBookingService.getEventBooking().getEvent().getId()
                        .equals(eventBookingService.getEventService().getEvent().getId())
                        && eventBookingService.getPriceAtPurchase().compareTo(eventBookingService.getEventService().getPrice()) == 0));
    }

    private void assertSavedLapTimesContainExpectedVehicle() {
        ArgumentCaptor<LapTime> lapTimeCaptor = ArgumentCaptor.forClass(LapTime.class);
        verify(lapTimeRepository, times(10)).save(lapTimeCaptor.capture());

        assertTrue(lapTimeCaptor.getAllValues().stream()
                .anyMatch(lapTime -> "Mazda MX-5 NA 1.8".equals(lapTime.getVehicle())));
    }

    private void assertUnreadMessageWasSeeded() {
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        Message savedMessage = messageCaptor.getValue();

        assertEquals("juanje", savedMessage.getSender().getDisplayName());
        assertEquals("trackevents", savedMessage.getReceiver().getDisplayName());
        assertEquals("Consulta sobre tandas en Jarama", savedMessage.getSubject());
        assertEquals(UNREAD_MESSAGE_CONTENT, savedMessage.getContent());
        assertFalse(savedMessage.getIsRead());
    }

    private void stubCoreRepositories(SeedState state) {
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        when(roleRepository.findByRoleName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.rolesByName.get(invocation.getArgument(0))));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(state.nextUserId.getAndIncrement());
            }
            state.usersByDisplayName.put(user.getDisplayName(), user);
            return user;
        });

        when(organizerRepository.findByLegalName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.organizersByLegalName.get(invocation.getArgument(0))));
        when(organizerRepository.save(any(Organizer.class))).thenAnswer(invocation -> {
            Organizer organizer = invocation.getArgument(0);
            if (organizer.getIdUser() == null && organizer.getUser() != null) {
                organizer.setIdUser(organizer.getUser().getId());
            }
            state.organizersByLegalName.put(organizer.getLegalName(), organizer);
            return organizer;
        });

        when(trackRepository.findByName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.tracksByName.get(invocation.getArgument(0))));

        when(serviceRepository.findByName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.servicesByName.get(invocation.getArgument(0))));
    }

    private void stubNestedSeedLookups(SeedState state) {
        when(userRepository.findByDisplayName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.usersByDisplayName.get(invocation.getArgument(0))));

        when(eventRepository.findByTrackIdAndEventDate(anyLong(), any(LocalDate.class)))
                .thenAnswer(invocation -> Optional.ofNullable(state.eventsByTrackAndDate.get(
                        state.trackDateKey(invocation.getArgument(0), invocation.getArgument(1))
                )));

        when(organizerServiceRepository.findByOrganizerIdUserAndServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.organizerServicesByPair.get(state.pairKey(
                        invocation.getArgument(0),
                        invocation.getArgument(1)
                ))));

        when(trackServiceRepository.findByTrackIdAndServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.trackServicesByPair.get(state.pairKey(
                        invocation.getArgument(0),
                        invocation.getArgument(1)
                ))));

        when(eventServiceRepository.findByEventIdAndTrackServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.eventServicesByEventAndTrackService.get(state.pairKey(
                        invocation.getArgument(0),
                        invocation.getArgument(1)
                ))));

        when(eventServiceRepository.findByEventIdAndOrganizerServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.eventServicesByEventAndOrganizerService.get(state.pairKey(
                        invocation.getArgument(0),
                        invocation.getArgument(1)
                ))));

        when(lapTimeRepository.findByUserIdAndTrackId(anyLong(), anyLong()))
                .thenAnswer(invocation -> new ArrayList<>(state.lapTimesByPair.getOrDefault(
                        state.pairKey(invocation.getArgument(0), invocation.getArgument(1)),
                        List.of()
                )));

        when(messageRepository.findConversation(anyLong(), anyLong()))
                .thenAnswer(invocation -> new ArrayList<>(state.conversationsByPair.getOrDefault(
                        state.pairKey(invocation.getArgument(0), invocation.getArgument(1)),
                        List.of()
                )));

        when(eventBookingRepository.findByUserIdAndEventId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.eventBookingsByUserAndEvent.get(
                        state.pairKey(invocation.getArgument(0), invocation.getArgument(1))
                )));

        when(eventBookingServiceRepository.findByEventBookingIdAndEventServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.eventBookingServicesByBookingAndEventService.get(
                        state.pairKey(invocation.getArgument(0), invocation.getArgument(1))
                )));
    }

    private void stubInitialCreationSaves(SeedState state) {
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            state.rolesByName.put(role.getRoleName(), role);
            return role;
        });

        when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> {
            Track track = invocation.getArgument(0);
            if (track.getId() == null) {
                track.setId(state.nextTrackId.getAndIncrement());
            }
            state.tracksByName.put(track.getName(), track);
            return track;
        });

        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> {
            Service service = invocation.getArgument(0);
            if (service.getId() == null) {
                service.setId(state.nextServiceId.getAndIncrement());
            }
            state.servicesByName.put(service.getName(), service);
            return service;
        });

    }

    private void stubRemainingSeedingSaves(SeedState state) {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            if (event.getId() == null) {
                event.setId(state.nextEventId.getAndIncrement());
            }
            state.eventsByTrackAndDate.put(state.trackDateKey(event.getTrack().getId(), event.getEventDate()), event);
            return event;
        });

        when(organizerServiceRepository.save(any(OrganizerService.class))).thenAnswer(invocation -> {
            OrganizerService organizerService = invocation.getArgument(0);
            if (organizerService.getId() == null) {
                organizerService.setId(state.nextOrganizerServiceId.getAndIncrement());
            }
            state.organizerServicesByPair.put(state.pairKey(
                    organizerService.getOrganizer().getIdUser(),
                    organizerService.getService().getId()
            ), organizerService);
            return organizerService;
        });

        when(trackServiceRepository.save(any(TrackService.class))).thenAnswer(invocation -> {
            TrackService trackService = invocation.getArgument(0);
            if (trackService.getId() == null) {
                trackService.setId(state.nextTrackServiceId.getAndIncrement());
            }
            state.trackServicesByPair.put(state.pairKey(
                    trackService.getTrack().getId(),
                    trackService.getService().getId()
            ), trackService);
            return trackService;
        });

        when(eventServiceRepository.save(any(EventService.class))).thenAnswer(invocation -> {
            EventService eventService = invocation.getArgument(0);
            if (eventService.getId() == null) {
                eventService.setId(state.nextEventServiceId.getAndIncrement());
            }
            state.addEventService(eventService);
            return eventService;
        });

        when(lapTimeRepository.save(any(LapTime.class))).thenAnswer(invocation -> {
            LapTime lapTime = invocation.getArgument(0);
            if (lapTime.getId() == null) {
                lapTime.setId(state.nextLapTimeId.getAndIncrement());
            }
            state.lapTimesByPair.computeIfAbsent(
                    state.pairKey(lapTime.getUser().getId(), lapTime.getTrack().getId()),
                    key -> new ArrayList<>()
            ).add(lapTime);
            return lapTime;
        });

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            if (message.getId() == null) {
                message.setId(state.nextMessageId.getAndIncrement());
            }
            state.conversationsByPair.computeIfAbsent(
                    state.pairKey(message.getSender().getId(), message.getReceiver().getId()),
                    key -> new ArrayList<>()
            ).add(message);
            return message;
        });

        when(eventBookingRepository.save(any(EventBooking.class))).thenAnswer(invocation -> {
            EventBooking eventBooking = invocation.getArgument(0);
            if (eventBooking.getId() == null) {
                eventBooking.setId(state.nextEventBookingId.getAndIncrement());
            }
            state.eventBookingsByUserAndEvent.put(
                    state.pairKey(eventBooking.getUser().getId(), eventBooking.getEvent().getId()),
                    eventBooking
            );
            return eventBooking;
        });

        when(eventBookingServiceRepository.save(any(EventBookingService.class))).thenAnswer(invocation -> {
            EventBookingService eventBookingService = invocation.getArgument(0);
            if (eventBookingService.getId() == null) {
                eventBookingService.setId(state.nextEventBookingServiceId.getAndIncrement());
            }
            state.addEventBookingService(eventBookingService);
            return eventBookingService;
        });
    }

    private void populateExistingDemoResources(SeedState state) {
        Role adminRole = role("ADMIN");
        Role userRole = role("USER");
        Role organizerRole = role("ORGANIZER");

        state.rolesByName.put("ADMIN", adminRole);
        state.rolesByName.put("USER", userRole);
        state.rolesByName.put("ORGANIZER", organizerRole);

        User admin = user(1L, "admin", adminRole);
        User juanje = user(2L, "juanje", userRole);
        User maria = user(3L, "maria", userRole);
        User carlos = user(4L, "carlos", userRole);
        User fernando = user(5L, "fernando.alonso", userRole);
        User alex = user(6L, "alex.palau", userRole);
        User trackevents = user(7L, "trackevents", organizerRole);
        User racingpro = user(8L, "racingpro", organizerRole);
        User iberianMotorsport = user(9L, "iberianmotorsport", organizerRole);

        state.usersByDisplayName.put(admin.getDisplayName(), admin);
        state.usersByDisplayName.put(juanje.getDisplayName(), juanje);
        state.usersByDisplayName.put(maria.getDisplayName(), maria);
        state.usersByDisplayName.put(carlos.getDisplayName(), carlos);
        state.usersByDisplayName.put(fernando.getDisplayName(), fernando);
        state.usersByDisplayName.put(alex.getDisplayName(), alex);
        state.usersByDisplayName.put(trackevents.getDisplayName(), trackevents);
        state.usersByDisplayName.put(racingpro.getDisplayName(), racingpro);
        state.usersByDisplayName.put(iberianMotorsport.getDisplayName(), iberianMotorsport);

        Organizer trackeventsOrganizer = organizer(trackevents, "TrackEvents S.L.");
        Organizer racingproOrganizer = organizer(racingpro, "RacingPro S.L.");
        Organizer iberianOrganizer = organizer(iberianMotorsport, "Iberian Motorsport Events S.L.");

        state.organizersByLegalName.put(trackeventsOrganizer.getLegalName(), trackeventsOrganizer);
        state.organizersByLegalName.put(racingproOrganizer.getLegalName(), racingproOrganizer);
        state.organizersByLegalName.put(iberianOrganizer.getLegalName(), iberianOrganizer);

        Track calafat = track(1L, "Circuit Calafat");
        Track jarama = track(2L, "Circuito de Madrid Jarama - RACE");
        Track ricardoTormo = track(3L, "Circuit Ricardo Tormo");
        Track guadix = track(4L, "Circuito Mike G Guadix");
        Track algarve = track(5L, "Autodromo Internacional do Algarve");
        Track nurburgring = track(6L, "Nurburgring");

        state.tracksByName.put(calafat.getName(), calafat);
        state.tracksByName.put(jarama.getName(), jarama);
        state.tracksByName.put(ricardoTormo.getName(), ricardoTormo);
        state.tracksByName.put(guadix.getName(), guadix);
        state.tracksByName.put(algarve.getName(), algarve);
        state.tracksByName.put(nurburgring.getName(), nurburgring);

        Service boxRental = service(1L, "Alquiler de box");
        Service coveredPaddock = service(2L, "Reserva de paddock cubierto");
        Service noiseControl = service(3L, "Control de ruido");
        Service skidpad = service(4L, "Pista deslizante");
        Service photography = service(5L, "Fotografia del evento");
        Service eventVideo = service(6L, "Video resumen del evento");
        Service catering = service(7L, "Catering para participantes");
        Service welcomePack = service(8L, "Welcome pack");
        Service instructor = service(9L, "Instructor de conduccion");
        Service secondDriverInsurance = service(10L, "Seguro para segundo conductor");
        Service copilotoInsurance = service(11L, "Seguro para copiloto");
        Service transponder = service(12L, "Cronometraje con transponder");

        state.servicesByName.put(boxRental.getName(), boxRental);
        state.servicesByName.put(coveredPaddock.getName(), coveredPaddock);
        state.servicesByName.put(noiseControl.getName(), noiseControl);
        state.servicesByName.put(skidpad.getName(), skidpad);
        state.servicesByName.put(photography.getName(), photography);
        state.servicesByName.put(eventVideo.getName(), eventVideo);
        state.servicesByName.put(catering.getName(), catering);
        state.servicesByName.put(welcomePack.getName(), welcomePack);
        state.servicesByName.put(instructor.getName(), instructor);
        state.servicesByName.put(secondDriverInsurance.getName(), secondDriverInsurance);
        state.servicesByName.put(copilotoInsurance.getName(), copilotoInsurance);
        state.servicesByName.put(transponder.getName(), transponder);

        OrganizerService trackeventsPhotography = organizerService(1L, trackeventsOrganizer, photography);
        OrganizerService trackeventsWelcomePack = organizerService(2L, trackeventsOrganizer, welcomePack);
        OrganizerService trackeventsTransponder = organizerService(3L, trackeventsOrganizer, transponder);
        OrganizerService trackeventsSecondDriver = organizerService(4L, trackeventsOrganizer, secondDriverInsurance);
        OrganizerService trackeventsCopilot = organizerService(5L, trackeventsOrganizer, copilotoInsurance);
        OrganizerService racingproVideo = organizerService(6L, racingproOrganizer, eventVideo);
        OrganizerService racingproCatering = organizerService(7L, racingproOrganizer, catering);
        OrganizerService racingproInstructor = organizerService(8L, racingproOrganizer, instructor);
        OrganizerService racingproTransponder = organizerService(9L, racingproOrganizer, transponder);
        OrganizerService racingproSecondDriver = organizerService(10L, racingproOrganizer, secondDriverInsurance);
        OrganizerService racingproCopilot = organizerService(11L, racingproOrganizer, copilotoInsurance);
        OrganizerService iberianPhotography = organizerService(12L, iberianOrganizer, photography);
        OrganizerService iberianWelcomePack = organizerService(13L, iberianOrganizer, welcomePack);
        OrganizerService iberianInstructor = organizerService(14L, iberianOrganizer, instructor);
        OrganizerService iberianTransponder = organizerService(15L, iberianOrganizer, transponder);
        OrganizerService iberianSecondDriver = organizerService(16L, iberianOrganizer, secondDriverInsurance);
        OrganizerService iberianCopilot = organizerService(17L, iberianOrganizer, copilotoInsurance);

        state.organizerServicesByPair.put(state.pairKey(trackevents.getId(), photography.getId()), trackeventsPhotography);
        state.organizerServicesByPair.put(state.pairKey(trackevents.getId(), welcomePack.getId()), trackeventsWelcomePack);
        state.organizerServicesByPair.put(state.pairKey(trackevents.getId(), transponder.getId()), trackeventsTransponder);
        state.organizerServicesByPair.put(state.pairKey(trackevents.getId(), secondDriverInsurance.getId()), trackeventsSecondDriver);
        state.organizerServicesByPair.put(state.pairKey(trackevents.getId(), copilotoInsurance.getId()), trackeventsCopilot);
        state.organizerServicesByPair.put(state.pairKey(racingpro.getId(), eventVideo.getId()), racingproVideo);
        state.organizerServicesByPair.put(state.pairKey(racingpro.getId(), catering.getId()), racingproCatering);
        state.organizerServicesByPair.put(state.pairKey(racingpro.getId(), instructor.getId()), racingproInstructor);
        state.organizerServicesByPair.put(state.pairKey(racingpro.getId(), transponder.getId()), racingproTransponder);
        state.organizerServicesByPair.put(state.pairKey(racingpro.getId(), secondDriverInsurance.getId()), racingproSecondDriver);
        state.organizerServicesByPair.put(state.pairKey(racingpro.getId(), copilotoInsurance.getId()), racingproCopilot);
        state.organizerServicesByPair.put(state.pairKey(iberianMotorsport.getId(), photography.getId()), iberianPhotography);
        state.organizerServicesByPair.put(state.pairKey(iberianMotorsport.getId(), welcomePack.getId()), iberianWelcomePack);
        state.organizerServicesByPair.put(state.pairKey(iberianMotorsport.getId(), instructor.getId()), iberianInstructor);
        state.organizerServicesByPair.put(state.pairKey(iberianMotorsport.getId(), transponder.getId()), iberianTransponder);
        state.organizerServicesByPair.put(state.pairKey(iberianMotorsport.getId(), secondDriverInsurance.getId()), iberianSecondDriver);
        state.organizerServicesByPair.put(state.pairKey(iberianMotorsport.getId(), copilotoInsurance.getId()), iberianCopilot);

        TrackService calafatBox = trackService(1L, calafat, boxRental);
        TrackService calafatNoise = trackService(2L, calafat, noiseControl);
        TrackService calafatSkidpad = trackService(3L, calafat, skidpad);
        TrackService jaramaBox = trackService(4L, jarama, boxRental);
        TrackService jaramaPaddock = trackService(5L, jarama, coveredPaddock);
        TrackService jaramaNoise = trackService(6L, jarama, noiseControl);
        TrackService jaramaSkidpad = trackService(7L, jarama, skidpad);
        TrackService jaramaTransponder = trackService(8L, jarama, transponder);
        TrackService ricardoBox = trackService(9L, ricardoTormo, boxRental);
        TrackService ricardoPaddock = trackService(10L, ricardoTormo, coveredPaddock);
        TrackService ricardoNoise = trackService(11L, ricardoTormo, noiseControl);
        TrackService ricardoTransponder = trackService(12L, ricardoTormo, transponder);
        TrackService guadixBox = trackService(13L, guadix, boxRental);
        TrackService guadixNoise = trackService(14L, guadix, noiseControl);
        TrackService guadixSkidpad = trackService(15L, guadix, skidpad);
        TrackService algarveBox = trackService(16L, algarve, boxRental);
        TrackService algarvePaddock = trackService(17L, algarve, coveredPaddock);
        TrackService algarveNoise = trackService(18L, algarve, noiseControl);
        TrackService algarveTransponder = trackService(19L, algarve, transponder);
        TrackService nurburgringBox = trackService(20L, nurburgring, boxRental);
        TrackService nurburgringPaddock = trackService(21L, nurburgring, coveredPaddock);
        TrackService nurburgringNoise = trackService(22L, nurburgring, noiseControl);
        TrackService nurburgringTransponder = trackService(23L, nurburgring, transponder);

        state.trackServicesByPair.put(state.pairKey(calafat.getId(), boxRental.getId()), calafatBox);
        state.trackServicesByPair.put(state.pairKey(calafat.getId(), noiseControl.getId()), calafatNoise);
        state.trackServicesByPair.put(state.pairKey(calafat.getId(), skidpad.getId()), calafatSkidpad);
        state.trackServicesByPair.put(state.pairKey(jarama.getId(), boxRental.getId()), jaramaBox);
        state.trackServicesByPair.put(state.pairKey(jarama.getId(), coveredPaddock.getId()), jaramaPaddock);
        state.trackServicesByPair.put(state.pairKey(jarama.getId(), noiseControl.getId()), jaramaNoise);
        state.trackServicesByPair.put(state.pairKey(jarama.getId(), skidpad.getId()), jaramaSkidpad);
        state.trackServicesByPair.put(state.pairKey(jarama.getId(), transponder.getId()), jaramaTransponder);
        state.trackServicesByPair.put(state.pairKey(ricardoTormo.getId(), boxRental.getId()), ricardoBox);
        state.trackServicesByPair.put(state.pairKey(ricardoTormo.getId(), coveredPaddock.getId()), ricardoPaddock);
        state.trackServicesByPair.put(state.pairKey(ricardoTormo.getId(), noiseControl.getId()), ricardoNoise);
        state.trackServicesByPair.put(state.pairKey(ricardoTormo.getId(), transponder.getId()), ricardoTransponder);
        state.trackServicesByPair.put(state.pairKey(guadix.getId(), boxRental.getId()), guadixBox);
        state.trackServicesByPair.put(state.pairKey(guadix.getId(), noiseControl.getId()), guadixNoise);
        state.trackServicesByPair.put(state.pairKey(guadix.getId(), skidpad.getId()), guadixSkidpad);
        state.trackServicesByPair.put(state.pairKey(algarve.getId(), boxRental.getId()), algarveBox);
        state.trackServicesByPair.put(state.pairKey(algarve.getId(), coveredPaddock.getId()), algarvePaddock);
        state.trackServicesByPair.put(state.pairKey(algarve.getId(), noiseControl.getId()), algarveNoise);
        state.trackServicesByPair.put(state.pairKey(algarve.getId(), transponder.getId()), algarveTransponder);
        state.trackServicesByPair.put(state.pairKey(nurburgring.getId(), boxRental.getId()), nurburgringBox);
        state.trackServicesByPair.put(state.pairKey(nurburgring.getId(), coveredPaddock.getId()), nurburgringPaddock);
        state.trackServicesByPair.put(state.pairKey(nurburgring.getId(), noiseControl.getId()), nurburgringNoise);
        state.trackServicesByPair.put(state.pairKey(nurburgring.getId(), transponder.getId()), nurburgringTransponder);

        Event jaramaEvent = event(1L, trackeventsOrganizer, jarama, LocalDate.of(2024, 4, 13), new BigDecimal("180.00"), 55);
        Event calafatEvent = event(2L, racingproOrganizer, calafat, LocalDate.of(2024, 6, 8), new BigDecimal("145.00"), 35);
        Event ricardoTormoEvent = event(3L,
                iberianOrganizer,
                ricardoTormo,
                LocalDate.of(2024, 10, 19),
                new BigDecimal("210.00"),
                70);
        Event algarveEvent = event(4L, trackeventsOrganizer, algarve, LocalDate.of(2025, 2, 22), new BigDecimal("260.00"), 45);
        LocalDate futureJaramaEventDate = LocalDate.now().plusDays(28);
        LocalDate futureCalafatEventDate = LocalDate.now().plusDays(49);
        LocalDate futureRicardoTormoEventDate = LocalDate.now().plusDays(77);
        LocalDate futureAlgarveEventDate = LocalDate.now().plusDays(112);
        Event futureJaramaEvent = event(5L,
                trackeventsOrganizer,
                jarama,
                futureJaramaEventDate,
                new BigDecimal("205.00"),
                60);
        Event futureCalafatEvent = event(6L,
                racingproOrganizer,
                calafat,
                futureCalafatEventDate,
                new BigDecimal("155.00"),
                36);
        Event futureRicardoTormoEvent = event(7L,
                iberianOrganizer,
                ricardoTormo,
                futureRicardoTormoEventDate,
                new BigDecimal("225.00"),
                72);
        Event futureAlgarveEvent = event(8L,
                trackeventsOrganizer,
                algarve,
                futureAlgarveEventDate,
                new BigDecimal("285.00"),
                48);

        state.addEvent(jaramaEvent);
        state.addEvent(calafatEvent);
        state.addEvent(ricardoTormoEvent);
        state.addEvent(algarveEvent);
        state.addEvent(futureJaramaEvent);
        state.addEvent(futureCalafatEvent);
        state.addEvent(futureRicardoTormoEvent);
        state.addEvent(futureAlgarveEvent);

        state.addEventBooking(eventBooking(1L, juanje, jaramaEvent, LocalDateTime.of(2024, 3, 20, 19, 0)));
        state.addEventBooking(eventBooking(2L, maria, jaramaEvent, LocalDateTime.of(2024, 3, 22, 10, 30)));
        state.addEventBooking(eventBooking(3L, fernando, jaramaEvent, LocalDateTime.of(2024, 3, 25, 18, 15)));
        state.addEventBooking(eventBooking(4L, carlos, calafatEvent, LocalDateTime.of(2024, 5, 14, 20, 0)));
        state.addEventBooking(eventBooking(5L, maria, calafatEvent, LocalDateTime.of(2024, 5, 16, 9, 45)));
        state.addEventBooking(eventBooking(6L, alex, ricardoTormoEvent, LocalDateTime.of(2024, 9, 18, 12, 0)));
        state.addEventBooking(eventBooking(7L, juanje, ricardoTormoEvent, LocalDateTime.of(2024, 9, 21, 19, 30)));
        state.addEventBooking(eventBooking(8L, fernando, algarveEvent, LocalDateTime.of(2025, 1, 23, 18, 40)));
        state.addEventBooking(eventBooking(9L, alex, algarveEvent, LocalDateTime.of(2025, 1, 25, 10, 10)));
        state.addEventBooking(eventBooking(10L, carlos, algarveEvent, LocalDateTime.of(2025, 1, 27, 21, 5)));
        state.addEventBooking(eventBooking(11L, juanje, futureJaramaEvent, LocalDateTime.now().minusDays(5)));
        state.addEventBooking(eventBooking(12L, maria, futureJaramaEvent, LocalDateTime.now().minusDays(4)));
        state.addEventBooking(eventBooking(13L, alex, futureJaramaEvent, LocalDateTime.now().minusDays(3)));
        state.addEventBooking(eventBooking(14L, carlos, futureCalafatEvent, LocalDateTime.now().minusDays(8)));
        state.addEventBooking(eventBooking(15L, fernando, futureCalafatEvent, LocalDateTime.now().minusDays(6)));
        state.addEventBooking(eventBooking(16L, alex, futureRicardoTormoEvent, LocalDateTime.now().minusDays(10)));
        state.addEventBooking(eventBooking(17L, juanje, futureRicardoTormoEvent, LocalDateTime.now().minusDays(9)));
        state.addEventBooking(eventBooking(18L, fernando, futureAlgarveEvent, LocalDateTime.now().minusDays(12)));
        state.addEventBooking(eventBooking(19L, maria, futureAlgarveEvent, LocalDateTime.now().minusDays(11)));
        state.addEventBooking(eventBooking(20L, carlos, futureAlgarveEvent, LocalDateTime.now().minusDays(7)));

        EventService jaramaBoxEventService = eventService(1L, jaramaEvent, jaramaBox, null, new BigDecimal("35.00"));
        EventService jaramaPaddockEventService = eventService(2L, jaramaEvent, jaramaPaddock, null, new BigDecimal("18.00"));
        EventService jaramaTransponderEventService = eventService(3L, jaramaEvent, jaramaTransponder, null, new BigDecimal("15.00"));
        EventService jaramaPhotographyEventService = eventService(4L, jaramaEvent, null, trackeventsPhotography, new BigDecimal("22.00"));
        EventService jaramaWelcomePackEventService = eventService(5L, jaramaEvent, null, trackeventsWelcomePack, new BigDecimal("10.00"));
        EventService jaramaSecondDriverEventService = eventService(6L, jaramaEvent, null, trackeventsSecondDriver, new BigDecimal("28.00"));
        EventService calafatSkidpadEventService = eventService(7L, calafatEvent, calafatSkidpad, null, new BigDecimal("25.00"));
        EventService calafatVideoEventService = eventService(8L, calafatEvent, null, racingproVideo, new BigDecimal("20.00"));
        EventService calafatInstructorEventService = eventService(9L, calafatEvent, null, racingproInstructor, new BigDecimal("45.00"));
        EventService calafatSecondDriverEventService = eventService(10L,
                calafatEvent,
                null,
                racingproSecondDriver,
                new BigDecimal("24.00"));
        EventService calafatCopilotEventService = eventService(11L, calafatEvent, null, racingproCopilot, new BigDecimal("15.00"));
        EventService ricardoBoxEventService = eventService(12L, ricardoTormoEvent, ricardoBox, null, new BigDecimal("40.00"));
        EventService ricardoPaddockEventService = eventService(13L,
                ricardoTormoEvent,
                ricardoPaddock,
                null,
                new BigDecimal("20.00"));
        EventService ricardoTransponderEventService = eventService(14L,
                ricardoTormoEvent,
                ricardoTransponder,
                null,
                new BigDecimal("18.00"));
        EventService ricardoPhotographyEventService = eventService(15L,
                ricardoTormoEvent,
                null,
                iberianPhotography,
                new BigDecimal("25.00"));
        EventService ricardoInstructorEventService = eventService(16L,
                ricardoTormoEvent,
                null,
                iberianInstructor,
                new BigDecimal("50.00"));
        EventService ricardoSecondDriverEventService = eventService(17L,
                ricardoTormoEvent,
                null,
                iberianSecondDriver,
                new BigDecimal("30.00"));
        EventService algarveBoxEventService = eventService(18L, algarveEvent, algarveBox, null, new BigDecimal("45.00"));
        EventService algarvePaddockEventService = eventService(19L, algarveEvent, algarvePaddock, null, new BigDecimal("25.00"));
        EventService algarveTransponderEventService = eventService(20L,
                algarveEvent,
                algarveTransponder,
                null,
                new BigDecimal("20.00"));
        EventService algarvePhotographyEventService = eventService(21L,
                algarveEvent,
                null,
                trackeventsPhotography,
                new BigDecimal("24.00"));
        EventService algarveWelcomePackEventService = eventService(22L,
                algarveEvent,
                null,
                trackeventsWelcomePack,
                new BigDecimal("12.00"));
        EventService algarveCopilotEventService = eventService(23L,
                algarveEvent,
                null,
                trackeventsCopilot,
                new BigDecimal("18.00"));
        EventService futureJaramaBoxEventService = eventService(24L,
                futureJaramaEvent,
                jaramaBox,
                null,
                new BigDecimal("38.00"));
        EventService futureJaramaPaddockEventService = eventService(25L,
                futureJaramaEvent,
                jaramaPaddock,
                null,
                new BigDecimal("20.00"));
        EventService futureJaramaTransponderEventService = eventService(26L,
                futureJaramaEvent,
                jaramaTransponder,
                null,
                new BigDecimal("16.00"));
        EventService futureJaramaPhotographyEventService = eventService(27L,
                futureJaramaEvent,
                null,
                trackeventsPhotography,
                new BigDecimal("24.00"));
        EventService futureJaramaWelcomePackEventService = eventService(28L,
                futureJaramaEvent,
                null,
                trackeventsWelcomePack,
                new BigDecimal("11.00"));
        EventService futureJaramaSecondDriverEventService = eventService(29L,
                futureJaramaEvent,
                null,
                trackeventsSecondDriver,
                new BigDecimal("30.00"));
        EventService futureCalafatSkidpadEventService = eventService(30L,
                futureCalafatEvent,
                calafatSkidpad,
                null,
                new BigDecimal("27.00"));
        EventService futureCalafatVideoEventService = eventService(31L,
                futureCalafatEvent,
                null,
                racingproVideo,
                new BigDecimal("22.00"));
        EventService futureCalafatInstructorEventService = eventService(32L,
                futureCalafatEvent,
                null,
                racingproInstructor,
                new BigDecimal("48.00"));
        EventService futureCalafatSecondDriverEventService = eventService(33L,
                futureCalafatEvent,
                null,
                racingproSecondDriver,
                new BigDecimal("25.00"));
        EventService futureCalafatCopilotEventService = eventService(34L,
                futureCalafatEvent,
                null,
                racingproCopilot,
                new BigDecimal("16.00"));
        EventService futureRicardoBoxEventService = eventService(35L,
                futureRicardoTormoEvent,
                ricardoBox,
                null,
                new BigDecimal("42.00"));
        EventService futureRicardoPaddockEventService = eventService(36L,
                futureRicardoTormoEvent,
                ricardoPaddock,
                null,
                new BigDecimal("22.00"));
        EventService futureRicardoTransponderEventService = eventService(37L,
                futureRicardoTormoEvent,
                ricardoTransponder,
                null,
                new BigDecimal("19.00"));
        EventService futureRicardoPhotographyEventService = eventService(38L,
                futureRicardoTormoEvent,
                null,
                iberianPhotography,
                new BigDecimal("27.00"));
        EventService futureRicardoInstructorEventService = eventService(39L,
                futureRicardoTormoEvent,
                null,
                iberianInstructor,
                new BigDecimal("52.00"));
        EventService futureRicardoSecondDriverEventService = eventService(40L,
                futureRicardoTormoEvent,
                null,
                iberianSecondDriver,
                new BigDecimal("31.00"));
        EventService futureAlgarveBoxEventService = eventService(41L,
                futureAlgarveEvent,
                algarveBox,
                null,
                new BigDecimal("48.00"));
        EventService futureAlgarvePaddockEventService = eventService(42L,
                futureAlgarveEvent,
                algarvePaddock,
                null,
                new BigDecimal("28.00"));
        EventService futureAlgarveTransponderEventService = eventService(43L,
                futureAlgarveEvent,
                algarveTransponder,
                null,
                new BigDecimal("22.00"));
        EventService futureAlgarvePhotographyEventService = eventService(44L,
                futureAlgarveEvent,
                null,
                trackeventsPhotography,
                new BigDecimal("26.00"));
        EventService futureAlgarveWelcomePackEventService = eventService(45L,
                futureAlgarveEvent,
                null,
                trackeventsWelcomePack,
                new BigDecimal("13.00"));
        EventService futureAlgarveCopilotEventService = eventService(46L,
                futureAlgarveEvent,
                null,
                trackeventsCopilot,
                new BigDecimal("20.00"));

        state.addEventService(jaramaBoxEventService);
        state.addEventService(jaramaPaddockEventService);
        state.addEventService(jaramaTransponderEventService);
        state.addEventService(jaramaPhotographyEventService);
        state.addEventService(jaramaWelcomePackEventService);
        state.addEventService(jaramaSecondDriverEventService);
        state.addEventService(calafatSkidpadEventService);
        state.addEventService(calafatVideoEventService);
        state.addEventService(calafatInstructorEventService);
        state.addEventService(calafatSecondDriverEventService);
        state.addEventService(calafatCopilotEventService);
        state.addEventService(ricardoBoxEventService);
        state.addEventService(ricardoPaddockEventService);
        state.addEventService(ricardoTransponderEventService);
        state.addEventService(ricardoPhotographyEventService);
        state.addEventService(ricardoInstructorEventService);
        state.addEventService(ricardoSecondDriverEventService);
        state.addEventService(algarveBoxEventService);
        state.addEventService(algarvePaddockEventService);
        state.addEventService(algarveTransponderEventService);
        state.addEventService(algarvePhotographyEventService);
        state.addEventService(algarveWelcomePackEventService);
        state.addEventService(algarveCopilotEventService);
        state.addEventService(futureJaramaBoxEventService);
        state.addEventService(futureJaramaPaddockEventService);
        state.addEventService(futureJaramaTransponderEventService);
        state.addEventService(futureJaramaPhotographyEventService);
        state.addEventService(futureJaramaWelcomePackEventService);
        state.addEventService(futureJaramaSecondDriverEventService);
        state.addEventService(futureCalafatSkidpadEventService);
        state.addEventService(futureCalafatVideoEventService);
        state.addEventService(futureCalafatInstructorEventService);
        state.addEventService(futureCalafatSecondDriverEventService);
        state.addEventService(futureCalafatCopilotEventService);
        state.addEventService(futureRicardoBoxEventService);
        state.addEventService(futureRicardoPaddockEventService);
        state.addEventService(futureRicardoTransponderEventService);
        state.addEventService(futureRicardoPhotographyEventService);
        state.addEventService(futureRicardoInstructorEventService);
        state.addEventService(futureRicardoSecondDriverEventService);
        state.addEventService(futureAlgarveBoxEventService);
        state.addEventService(futureAlgarvePaddockEventService);
        state.addEventService(futureAlgarveTransponderEventService);
        state.addEventService(futureAlgarvePhotographyEventService);
        state.addEventService(futureAlgarveWelcomePackEventService);
        state.addEventService(futureAlgarveCopilotEventService);

        state.addEventBookingService(eventBookingService(1L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(juanje.getId(), jaramaEvent.getId())),
                jaramaTransponderEventService));
        state.addEventBookingService(eventBookingService(2L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(juanje.getId(), jaramaEvent.getId())),
                jaramaWelcomePackEventService));
        state.addEventBookingService(eventBookingService(3L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(maria.getId(), jaramaEvent.getId())),
                jaramaPhotographyEventService));
        state.addEventBookingService(eventBookingService(4L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(fernando.getId(), jaramaEvent.getId())),
                jaramaBoxEventService));
        state.addEventBookingService(eventBookingService(5L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(fernando.getId(), jaramaEvent.getId())),
                jaramaSecondDriverEventService));
        state.addEventBookingService(eventBookingService(6L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(carlos.getId(), calafatEvent.getId())),
                calafatSkidpadEventService));
        state.addEventBookingService(eventBookingService(7L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(maria.getId(), calafatEvent.getId())),
                calafatInstructorEventService));
        state.addEventBookingService(eventBookingService(8L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(maria.getId(), calafatEvent.getId())),
                calafatCopilotEventService));
        state.addEventBookingService(eventBookingService(9L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(alex.getId(), ricardoTormoEvent.getId())),
                ricardoBoxEventService));
        state.addEventBookingService(eventBookingService(10L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(alex.getId(), ricardoTormoEvent.getId())),
                ricardoPhotographyEventService));
        state.addEventBookingService(eventBookingService(11L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(juanje.getId(), ricardoTormoEvent.getId())),
                ricardoInstructorEventService));
        state.addEventBookingService(eventBookingService(12L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(juanje.getId(), ricardoTormoEvent.getId())),
                ricardoSecondDriverEventService));
        state.addEventBookingService(eventBookingService(13L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(fernando.getId(), algarveEvent.getId())),
                algarveBoxEventService));
        state.addEventBookingService(eventBookingService(14L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(fernando.getId(), algarveEvent.getId())),
                algarveTransponderEventService));
        state.addEventBookingService(eventBookingService(15L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(alex.getId(), algarveEvent.getId())),
                algarvePhotographyEventService));
        state.addEventBookingService(eventBookingService(16L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(carlos.getId(), algarveEvent.getId())),
                algarveWelcomePackEventService));
        state.addEventBookingService(eventBookingService(17L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(carlos.getId(), algarveEvent.getId())),
                algarveCopilotEventService));
        state.addEventBookingService(eventBookingService(18L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(juanje.getId(), futureJaramaEvent.getId())),
                futureJaramaTransponderEventService));
        state.addEventBookingService(eventBookingService(19L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(juanje.getId(), futureJaramaEvent.getId())),
                futureJaramaWelcomePackEventService));
        state.addEventBookingService(eventBookingService(20L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(maria.getId(), futureJaramaEvent.getId())),
                futureJaramaPhotographyEventService));
        state.addEventBookingService(eventBookingService(21L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(alex.getId(), futureJaramaEvent.getId())),
                futureJaramaBoxEventService));
        state.addEventBookingService(eventBookingService(22L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(alex.getId(), futureJaramaEvent.getId())),
                futureJaramaSecondDriverEventService));
        state.addEventBookingService(eventBookingService(23L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(carlos.getId(), futureCalafatEvent.getId())),
                futureCalafatSkidpadEventService));
        state.addEventBookingService(eventBookingService(24L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(fernando.getId(), futureCalafatEvent.getId())),
                futureCalafatInstructorEventService));
        state.addEventBookingService(eventBookingService(25L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(fernando.getId(), futureCalafatEvent.getId())),
                futureCalafatCopilotEventService));
        state.addEventBookingService(eventBookingService(26L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(alex.getId(), futureRicardoTormoEvent.getId())),
                futureRicardoBoxEventService));
        state.addEventBookingService(eventBookingService(27L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(alex.getId(), futureRicardoTormoEvent.getId())),
                futureRicardoPhotographyEventService));
        state.addEventBookingService(eventBookingService(28L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(juanje.getId(), futureRicardoTormoEvent.getId())),
                futureRicardoInstructorEventService));
        state.addEventBookingService(eventBookingService(29L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(juanje.getId(), futureRicardoTormoEvent.getId())),
                futureRicardoSecondDriverEventService));
        state.addEventBookingService(eventBookingService(30L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(fernando.getId(), futureAlgarveEvent.getId())),
                futureAlgarveBoxEventService));
        state.addEventBookingService(eventBookingService(31L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(fernando.getId(), futureAlgarveEvent.getId())),
                futureAlgarveTransponderEventService));
        state.addEventBookingService(eventBookingService(32L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(maria.getId(), futureAlgarveEvent.getId())),
                futureAlgarvePhotographyEventService));
        state.addEventBookingService(eventBookingService(33L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(carlos.getId(), futureAlgarveEvent.getId())),
                futureAlgarveWelcomePackEventService));
        state.addEventBookingService(eventBookingService(34L,
                state.eventBookingsByUserAndEvent.get(state.pairKey(carlos.getId(), futureAlgarveEvent.getId())),
                futureAlgarveCopilotEventService));

        state.addLapTime(lapTime(fernando, jarama, LocalDate.of(2026, 3, 8), 107215L, "Alpine A110 R"));
        state.addLapTime(lapTime(juanje, jarama, LocalDate.of(2026, 3, 8), 111842L, "BMW M2"));
        state.addLapTime(lapTime(maria, jarama, LocalDate.of(2026, 3, 8), 118530L, "Toyota GR86"));
        state.addLapTime(lapTime(alex, ricardoTormo, LocalDate.of(2026, 3, 12), 102480L, "Porsche 911 GT3"));
        state.addLapTime(lapTime(carlos, ricardoTormo, LocalDate.of(2026, 3, 12), 111965L, "MINI John Cooper Works"));
        state.addLapTime(lapTime(maria, calafat, LocalDate.of(2026, 3, 15), 95620L, "Hyundai i30 N"));
        state.addLapTime(lapTime(juanje, calafat, LocalDate.of(2026, 3, 15), 98640L, "Mazda MX-5 NA 1.8"));
        state.addLapTime(lapTime(fernando, guadix, LocalDate.of(2026, 3, 18), 101870L, "Alpine A110 R"));
        state.addLapTime(lapTime(alex, guadix, LocalDate.of(2026, 3, 18), 104450L, "CUPRA Leon VZ"));
        state.addLapTime(lapTime(juanje, algarve, LocalDate.of(2026, 3, 20), 121930L, "Porsche Cayman S"));

        state.addMessage(message(
                juanje,
                trackevents,
                LocalDateTime.of(2026, 3, 24, 18, 30),
                false,
                "Consulta sobre tandas en Jarama",
                UNREAD_MESSAGE_CONTENT
        ));
    }

    private Role role(String roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
        return role;
    }

    private User user(Long id, String displayName, Role role) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setRole(role);
        return user;
    }

    private Organizer organizer(User user, String legalName) {
        Organizer organizer = new Organizer();
        organizer.setUser(user);
        organizer.setIdUser(user.getId());
        organizer.setLegalName(legalName);
        return organizer;
    }

    private Track track(Long id, String name) {
        Track track = new Track();
        track.setId(id);
        track.setName(name);
        return track;
    }

    private Service service(Long id, String name) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        return service;
    }

    private Event event(Long id,
                        Organizer organizer,
                        Track track,
                        LocalDate eventDate,
                        BigDecimal basePrice,
                        Integer maxParticipants) {
        Event event = new Event();
        event.setId(id);
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(eventDate);
        event.setBasePrice(basePrice);
        event.setMaxParticipants(maxParticipants);
        return event;
    }

    private OrganizerService organizerService(Long id, Organizer organizer, Service service) {
        OrganizerService organizerService = new OrganizerService();
        organizerService.setId(id);
        organizerService.setOrganizer(organizer);
        organizerService.setService(service);
        return organizerService;
    }

    private TrackService trackService(Long id, Track track, Service service) {
        TrackService trackService = new TrackService();
        trackService.setId(id);
        trackService.setTrack(track);
        trackService.setService(service);
        return trackService;
    }

    private EventService eventService(Long id,
                                      Event event,
                                      TrackService trackService,
                                      OrganizerService organizerService,
                                      BigDecimal price) {
        EventService eventService = new EventService();
        eventService.setId(id);
        eventService.setEvent(event);
        eventService.setTrackService(trackService);
        eventService.setOrganizerService(organizerService);
        eventService.setPrice(price);
        return eventService;
    }

    private LapTime lapTime(User user, Track track, LocalDate lapDate, Long lapTimeMs, String vehicle) {
        LapTime lapTime = new LapTime();
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(lapDate);
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setVehicle(vehicle);
        return lapTime;
    }

    private Message message(User sender,
                            User receiver,
                            LocalDateTime sentAt,
                            boolean isRead,
                            String subject,
                            String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentAt(sentAt);
        message.setIsRead(isRead);
        message.setSubject(subject);
        message.setContent(content);
        return message;
    }

    private EventBooking eventBooking(Long id, User user, Event event, LocalDateTime bookedAt) {
        EventBooking eventBooking = new EventBooking();
        eventBooking.setId(id);
        eventBooking.setUser(user);
        eventBooking.setEvent(event);
        eventBooking.setBookedAt(bookedAt);
        eventBooking.setBasePriceAtPurchase(event.getBasePrice());
        return eventBooking;
    }

    private EventBookingService eventBookingService(Long id, EventBooking eventBooking, EventService eventService) {
        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setId(id);
        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(eventService);
        eventBookingService.setPriceAtPurchase(eventService.getPrice());
        return eventBookingService;
    }

    private static final class SeedState {
        private final Map<String, Role> rolesByName = new HashMap<>();
        private final Map<String, User> usersByDisplayName = new HashMap<>();
        private final Map<String, Organizer> organizersByLegalName = new HashMap<>();
        private final Map<String, Track> tracksByName = new HashMap<>();
        private final Map<String, Service> servicesByName = new HashMap<>();
        private final Map<String, Event> eventsByTrackAndDate = new HashMap<>();
        private final Map<String, EventBooking> eventBookingsByUserAndEvent = new HashMap<>();
        private final Map<String, OrganizerService> organizerServicesByPair = new HashMap<>();
        private final Map<String, TrackService> trackServicesByPair = new HashMap<>();
        private final Map<String, EventService> eventServicesByEventAndTrackService = new HashMap<>();
        private final Map<String, EventService> eventServicesByEventAndOrganizerService = new HashMap<>();
        private final Map<String, EventBookingService> eventBookingServicesByBookingAndEventService = new HashMap<>();
        private final Map<String, List<LapTime>> lapTimesByPair = new HashMap<>();
        private final Map<String, List<Message>> conversationsByPair = new HashMap<>();
        private final AtomicLong nextUserId = new AtomicLong(1L);
        private final AtomicLong nextTrackId = new AtomicLong(1L);
        private final AtomicLong nextServiceId = new AtomicLong(1L);
        private final AtomicLong nextOrganizerServiceId = new AtomicLong(1L);
        private final AtomicLong nextTrackServiceId = new AtomicLong(1L);
        private final AtomicLong nextEventId = new AtomicLong(1L);
        private final AtomicLong nextEventBookingId = new AtomicLong(1L);
        private final AtomicLong nextEventServiceId = new AtomicLong(1L);
        private final AtomicLong nextEventBookingServiceId = new AtomicLong(1L);
        private final AtomicLong nextLapTimeId = new AtomicLong(1L);
        private final AtomicLong nextMessageId = new AtomicLong(1L);

        private String pairKey(Long leftId, Long rightId) {
            return leftId + "-" + rightId;
        }

        private String trackDateKey(Long trackId, LocalDate eventDate) {
            return trackId + "-" + eventDate;
        }

        private void addEvent(Event event) {
            eventsByTrackAndDate.put(trackDateKey(event.getTrack().getId(), event.getEventDate()), event);
        }

        private void addEventBooking(EventBooking eventBooking) {
            eventBookingsByUserAndEvent.put(
                    pairKey(eventBooking.getUser().getId(), eventBooking.getEvent().getId()),
                    eventBooking
            );
        }

        private void addEventService(EventService eventService) {
            if (eventService.getTrackService() != null) {
                eventServicesByEventAndTrackService.put(
                        pairKey(eventService.getEvent().getId(), eventService.getTrackService().getId()),
                        eventService
                );
            }
            if (eventService.getOrganizerService() != null) {
                eventServicesByEventAndOrganizerService.put(
                        pairKey(eventService.getEvent().getId(), eventService.getOrganizerService().getId()),
                        eventService
                );
            }
        }

        private void addEventBookingService(EventBookingService eventBookingService) {
            eventBookingServicesByBookingAndEventService.put(
                    pairKey(eventBookingService.getEventBooking().getId(), eventBookingService.getEventService().getId()),
                    eventBookingService
            );
        }

        private void addLapTime(LapTime lapTime) {
            lapTimesByPair.computeIfAbsent(pairKey(lapTime.getUser().getId(), lapTime.getTrack().getId()),
                    key -> new ArrayList<>()).add(lapTime);
        }

        private void addMessage(Message message) {
            conversationsByPair.computeIfAbsent(pairKey(message.getSender().getId(), message.getReceiver().getId()),
                    key -> new ArrayList<>()).add(message);
        }
    }
}
