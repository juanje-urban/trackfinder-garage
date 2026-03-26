package com.trackfindergarage.backend.infrastructure.config;

import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.domain.model.User;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataEventBookingRepository;
import com.trackfindergarage.backend.infrastructure.adapter.out.persistence.SpringDataEventRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

        verify(roleRepository, times(3)).save(any(Role.class));
        verify(userRepository, times(9)).save(any(User.class));
        verify(organizerRepository, times(3)).save(any(Organizer.class));
        verify(trackRepository, times(6)).save(any(Track.class));
        verify(serviceRepository, times(12)).save(any(Service.class));
        verify(organizerServiceRepository, times(17)).save(any(OrganizerService.class));
        verify(trackServiceRepository, times(23)).save(any(TrackService.class));
        verify(eventRepository, times(4)).save(any(Event.class));
        verify(eventBookingRepository, times(10)).save(any(EventBooking.class));
        verify(lapTimeRepository, times(10)).save(any(LapTime.class));
        verify(messageRepository, times(1)).save(any(Message.class));

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(4)).save(eventCaptor.capture());
        assertTrue(eventCaptor.getAllValues().stream()
                .allMatch(event -> event.getEventDate().isBefore(LocalDate.now())));

        ArgumentCaptor<EventBooking> eventBookingCaptor = ArgumentCaptor.forClass(EventBooking.class);
        verify(eventBookingRepository, times(10)).save(eventBookingCaptor.capture());
        assertTrue(eventBookingCaptor.getAllValues().stream()
                .allMatch(eventBooking -> "USER".equals(eventBooking.getUser().getRole().getRoleName())));

        ArgumentCaptor<LapTime> lapTimeCaptor = ArgumentCaptor.forClass(LapTime.class);
        verify(lapTimeRepository, times(10)).save(lapTimeCaptor.capture());
        assertTrue(lapTimeCaptor.getAllValues().stream()
                .anyMatch(lapTime -> "Mazda MX-5 NA 1.8".equals(lapTime.getVehicle())));

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        Message savedMessage = messageCaptor.getValue();

        assertEquals("juanje", savedMessage.getSender().getDisplayName());
        assertEquals("trackevents", savedMessage.getReceiver().getDisplayName());
        assertEquals("Consulta sobre tandas en Jarama", savedMessage.getSubject());
        assertEquals(UNREAD_MESSAGE_CONTENT, savedMessage.getContent());
        assertFalse(savedMessage.getIsRead());
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
        verify(eventBookingRepository, never()).save(any(EventBooking.class));
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
                .thenAnswer(invocation -> state.organizerServiceKeys.contains(state.pairKey(
                                invocation.getArgument(0),
                                invocation.getArgument(1)))
                        ? Optional.of(new OrganizerService())
                        : Optional.empty());

        when(trackServiceRepository.findByTrackIdAndServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> state.trackServiceKeys.contains(state.pairKey(
                                invocation.getArgument(0),
                                invocation.getArgument(1)))
                        ? Optional.of(new TrackService())
                        : Optional.empty());

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
            state.organizerServiceKeys.add(state.pairKey(
                    organizerService.getOrganizer().getIdUser(),
                    organizerService.getService().getId()
            ));
            return organizerService;
        });

        when(trackServiceRepository.save(any(TrackService.class))).thenAnswer(invocation -> {
            TrackService trackService = invocation.getArgument(0);
            state.trackServiceKeys.add(state.pairKey(
                    trackService.getTrack().getId(),
                    trackService.getService().getId()
            ));
            return trackService;
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

        state.organizersByLegalName.put("TrackEvents S.L.", organizer(trackevents, "TrackEvents S.L."));
        state.organizersByLegalName.put("RacingPro S.L.", organizer(racingpro, "RacingPro S.L."));
        state.organizersByLegalName.put("Iberian Motorsport Events S.L.", organizer(iberianMotorsport, "Iberian Motorsport Events S.L."));

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

        state.servicesByName.put("Alquiler de box", service(1L, "Alquiler de box"));
        state.servicesByName.put("Reserva de paddock cubierto", service(2L, "Reserva de paddock cubierto"));
        state.servicesByName.put("Control de ruido", service(3L, "Control de ruido"));
        state.servicesByName.put("Pista deslizante", service(4L, "Pista deslizante"));
        state.servicesByName.put("Fotografia del evento", service(5L, "Fotografia del evento"));
        state.servicesByName.put("Video resumen del evento", service(6L, "Video resumen del evento"));
        state.servicesByName.put("Catering para participantes", service(7L, "Catering para participantes"));
        state.servicesByName.put("Welcome pack", service(8L, "Welcome pack"));
        state.servicesByName.put("Instructor de conduccion", service(9L, "Instructor de conduccion"));
        state.servicesByName.put("Seguro para segundo conductor", service(10L, "Seguro para segundo conductor"));
        state.servicesByName.put("Seguro para copiloto", service(11L, "Seguro para copiloto"));
        state.servicesByName.put("Cronometraje con transponder", service(12L, "Cronometraje con transponder"));

        state.organizerServiceKeys.add(state.pairKey(trackevents.getId(), 5L));
        state.organizerServiceKeys.add(state.pairKey(trackevents.getId(), 8L));
        state.organizerServiceKeys.add(state.pairKey(trackevents.getId(), 12L));
        state.organizerServiceKeys.add(state.pairKey(trackevents.getId(), 10L));
        state.organizerServiceKeys.add(state.pairKey(trackevents.getId(), 11L));
        state.organizerServiceKeys.add(state.pairKey(racingpro.getId(), 6L));
        state.organizerServiceKeys.add(state.pairKey(racingpro.getId(), 7L));
        state.organizerServiceKeys.add(state.pairKey(racingpro.getId(), 9L));
        state.organizerServiceKeys.add(state.pairKey(racingpro.getId(), 12L));
        state.organizerServiceKeys.add(state.pairKey(racingpro.getId(), 10L));
        state.organizerServiceKeys.add(state.pairKey(racingpro.getId(), 11L));
        state.organizerServiceKeys.add(state.pairKey(iberianMotorsport.getId(), 5L));
        state.organizerServiceKeys.add(state.pairKey(iberianMotorsport.getId(), 8L));
        state.organizerServiceKeys.add(state.pairKey(iberianMotorsport.getId(), 9L));
        state.organizerServiceKeys.add(state.pairKey(iberianMotorsport.getId(), 12L));
        state.organizerServiceKeys.add(state.pairKey(iberianMotorsport.getId(), 10L));
        state.organizerServiceKeys.add(state.pairKey(iberianMotorsport.getId(), 11L));

        state.trackServiceKeys.add(state.pairKey(calafat.getId(), 1L));
        state.trackServiceKeys.add(state.pairKey(calafat.getId(), 3L));
        state.trackServiceKeys.add(state.pairKey(calafat.getId(), 4L));
        state.trackServiceKeys.add(state.pairKey(jarama.getId(), 1L));
        state.trackServiceKeys.add(state.pairKey(jarama.getId(), 2L));
        state.trackServiceKeys.add(state.pairKey(jarama.getId(), 3L));
        state.trackServiceKeys.add(state.pairKey(jarama.getId(), 4L));
        state.trackServiceKeys.add(state.pairKey(jarama.getId(), 12L));
        state.trackServiceKeys.add(state.pairKey(ricardoTormo.getId(), 1L));
        state.trackServiceKeys.add(state.pairKey(ricardoTormo.getId(), 2L));
        state.trackServiceKeys.add(state.pairKey(ricardoTormo.getId(), 3L));
        state.trackServiceKeys.add(state.pairKey(ricardoTormo.getId(), 12L));
        state.trackServiceKeys.add(state.pairKey(guadix.getId(), 1L));
        state.trackServiceKeys.add(state.pairKey(guadix.getId(), 3L));
        state.trackServiceKeys.add(state.pairKey(guadix.getId(), 4L));
        state.trackServiceKeys.add(state.pairKey(algarve.getId(), 1L));
        state.trackServiceKeys.add(state.pairKey(algarve.getId(), 2L));
        state.trackServiceKeys.add(state.pairKey(algarve.getId(), 3L));
        state.trackServiceKeys.add(state.pairKey(algarve.getId(), 12L));
        state.trackServiceKeys.add(state.pairKey(nurburgring.getId(), 1L));
        state.trackServiceKeys.add(state.pairKey(nurburgring.getId(), 2L));
        state.trackServiceKeys.add(state.pairKey(nurburgring.getId(), 3L));
        state.trackServiceKeys.add(state.pairKey(nurburgring.getId(), 12L));

        Event jaramaEvent = event(1L,
                organizer(trackevents, "TrackEvents S.L."),
                jarama,
                LocalDate.of(2024, 4, 13),
                new BigDecimal("180.00"),
                55);
        Event calafatEvent = event(2L,
                organizer(racingpro, "RacingPro S.L."),
                calafat,
                LocalDate.of(2024, 6, 8),
                new BigDecimal("145.00"),
                35);
        Event ricardoTormoEvent = event(3L,
                organizer(iberianMotorsport, "Iberian Motorsport Events S.L."),
                ricardoTormo,
                LocalDate.of(2024, 10, 19),
                new BigDecimal("210.00"),
                70);
        Event algarveEvent = event(4L,
                organizer(trackevents, "TrackEvents S.L."),
                algarve,
                LocalDate.of(2025, 2, 22),
                new BigDecimal("260.00"),
                45);

        state.addEvent(jaramaEvent);
        state.addEvent(calafatEvent);
        state.addEvent(ricardoTormoEvent);
        state.addEvent(algarveEvent);

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

    private static final class SeedState {
        private final Map<String, Role> rolesByName = new HashMap<>();
        private final Map<String, User> usersByDisplayName = new HashMap<>();
        private final Map<String, Organizer> organizersByLegalName = new HashMap<>();
        private final Map<String, Track> tracksByName = new HashMap<>();
        private final Map<String, Service> servicesByName = new HashMap<>();
        private final Map<String, Event> eventsByTrackAndDate = new HashMap<>();
        private final Map<String, EventBooking> eventBookingsByUserAndEvent = new HashMap<>();
        private final Set<String> organizerServiceKeys = new HashSet<>();
        private final Set<String> trackServiceKeys = new HashSet<>();
        private final Map<String, List<LapTime>> lapTimesByPair = new HashMap<>();
        private final Map<String, List<Message>> conversationsByPair = new HashMap<>();
        private final AtomicLong nextUserId = new AtomicLong(1L);
        private final AtomicLong nextTrackId = new AtomicLong(1L);
        private final AtomicLong nextServiceId = new AtomicLong(1L);
        private final AtomicLong nextEventId = new AtomicLong(1L);
        private final AtomicLong nextEventBookingId = new AtomicLong(1L);
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
