package com.trackfindergarage.backend.infrastructure.config;

import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Message;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.domain.model.User;
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
        stubRepositories(state);

        demoDataSeeder.run();

        verify(roleRepository, times(3)).save(any(Role.class));
        verify(userRepository, times(9)).save(any(User.class));
        verify(organizerRepository, times(3)).save(any(Organizer.class));
        verify(trackRepository, times(6)).save(any(Track.class));
        verify(serviceRepository, times(12)).save(any(Service.class));
        verify(organizerServiceRepository, times(17)).save(any(OrganizerService.class));
        verify(trackServiceRepository, times(23)).save(any(TrackService.class));
        verify(lapTimeRepository, times(10)).save(any(LapTime.class));
        verify(messageRepository, times(1)).save(any(Message.class));

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
        stubRepositories(state);
        populateExistingDemoResources(state);

        when(organizerServiceRepository.findByOrganizerIdUserAndServiceId(anyLong(), anyLong()))
                .thenReturn(Optional.of(new OrganizerService()));
        when(trackServiceRepository.findByTrackIdAndServiceId(anyLong(), anyLong()))
                .thenReturn(Optional.of(new TrackService()));

        demoDataSeeder.run();

        verify(roleRepository, never()).save(any(Role.class));
        verify(trackRepository, never()).save(any(Track.class));
        verify(serviceRepository, never()).save(any(Service.class));
        verify(organizerServiceRepository, never()).save(any(OrganizerService.class));
        verify(trackServiceRepository, never()).save(any(TrackService.class));
        verify(lapTimeRepository, never()).save(any(LapTime.class));
        verify(messageRepository, never()).save(any(Message.class));
    }

    private void stubRepositories(SeedState state) {
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        when(roleRepository.findByRoleName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.rolesByName.get(invocation.getArgument(0))));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            state.rolesByName.put(role.getRoleName(), role);
            return role;
        });

        when(userRepository.findByDisplayName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.usersByDisplayName.get(invocation.getArgument(0))));
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
        when(trackRepository.save(any(Track.class))).thenAnswer(invocation -> {
            Track track = invocation.getArgument(0);
            if (track.getId() == null) {
                track.setId(state.nextTrackId.getAndIncrement());
            }
            state.tracksByName.put(track.getName(), track);
            return track;
        });

        when(serviceRepository.findByName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.servicesByName.get(invocation.getArgument(0))));
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> {
            Service service = invocation.getArgument(0);
            if (service.getId() == null) {
                service.setId(state.nextServiceId.getAndIncrement());
            }
            state.servicesByName.put(service.getName(), service);
            return service;
        });

        when(organizerServiceRepository.findByOrganizerIdUserAndServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> state.organizerServiceKeys.contains(state.pairKey(
                                invocation.getArgument(0),
                                invocation.getArgument(1)))
                        ? Optional.of(new OrganizerService())
                        : Optional.empty());
        when(organizerServiceRepository.save(any(OrganizerService.class))).thenAnswer(invocation -> {
            OrganizerService organizerService = invocation.getArgument(0);
            state.organizerServiceKeys.add(state.pairKey(
                    organizerService.getOrganizer().getIdUser(),
                    organizerService.getService().getId()
            ));
            return organizerService;
        });

        when(trackServiceRepository.findByTrackIdAndServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> state.trackServiceKeys.contains(state.pairKey(
                                invocation.getArgument(0),
                                invocation.getArgument(1)))
                        ? Optional.of(new TrackService())
                        : Optional.empty());
        when(trackServiceRepository.save(any(TrackService.class))).thenAnswer(invocation -> {
            TrackService trackService = invocation.getArgument(0);
            state.trackServiceKeys.add(state.pairKey(
                    trackService.getTrack().getId(),
                    trackService.getService().getId()
            ));
            return trackService;
        });

        when(lapTimeRepository.findByUserIdAndTrackId(anyLong(), anyLong()))
                .thenAnswer(invocation -> new ArrayList<>(state.lapTimesByPair.getOrDefault(
                        state.pairKey(invocation.getArgument(0), invocation.getArgument(1)),
                        List.of()
                )));
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

        when(messageRepository.findConversation(anyLong(), anyLong()))
                .thenAnswer(invocation -> new ArrayList<>(state.conversationsByPair.getOrDefault(
                        state.pairKey(invocation.getArgument(0), invocation.getArgument(1)),
                        List.of()
                )));
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
    }

    private void populateExistingDemoResources(SeedState state) {
        state.rolesByName.put("ADMIN", role("ADMIN"));
        state.rolesByName.put("USER", role("USER"));
        state.rolesByName.put("ORGANIZER", role("ORGANIZER"));

        User juanje = user(1L, "juanje");
        User maria = user(2L, "maria");
        User carlos = user(3L, "carlos");
        User fernando = user(4L, "fernando.alonso");
        User alex = user(5L, "alex.palau");
        User trackevents = user(6L, "trackevents");

        state.usersByDisplayName.put(juanje.getDisplayName(), juanje);
        state.usersByDisplayName.put(maria.getDisplayName(), maria);
        state.usersByDisplayName.put(carlos.getDisplayName(), carlos);
        state.usersByDisplayName.put(fernando.getDisplayName(), fernando);
        state.usersByDisplayName.put(alex.getDisplayName(), alex);
        state.usersByDisplayName.put(trackevents.getDisplayName(), trackevents);

        state.organizersByLegalName.put("TrackEvents S.L.", organizer(trackevents, "TrackEvents S.L."));
        state.organizersByLegalName.put("RacingPro S.L.", organizer(user(7L, "racingpro"), "RacingPro S.L."));
        state.organizersByLegalName.put("Iberian Motorsport Events S.L.", organizer(user(8L, "iberianmotorsport"), "Iberian Motorsport Events S.L."));

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

    private User user(Long id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
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

    private static final class SeedState {
        private final Map<String, Role> rolesByName = new HashMap<>();
        private final Map<String, User> usersByDisplayName = new HashMap<>();
        private final Map<String, Organizer> organizersByLegalName = new HashMap<>();
        private final Map<String, Track> tracksByName = new HashMap<>();
        private final Map<String, Service> servicesByName = new HashMap<>();
        private final Set<String> organizerServiceKeys = new HashSet<>();
        private final Set<String> trackServiceKeys = new HashSet<>();
        private final Map<String, List<LapTime>> lapTimesByPair = new HashMap<>();
        private final Map<String, List<Message>> conversationsByPair = new HashMap<>();
        private final AtomicLong nextUserId = new AtomicLong(1L);
        private final AtomicLong nextTrackId = new AtomicLong(1L);
        private final AtomicLong nextServiceId = new AtomicLong(1L);
        private final AtomicLong nextLapTimeId = new AtomicLong(1L);
        private final AtomicLong nextMessageId = new AtomicLong(1L);

        private String pairKey(Long leftId, Long rightId) {
            return leftId + "-" + rightId;
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
