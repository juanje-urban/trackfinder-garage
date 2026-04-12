package com.trackfindergarage.backend.infrastructure.config;

import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Message;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    private static final String USER_ROLE_NAME = "USER";
    private static final String ORGANIZER_ROLE_NAME = "ORGANIZER";
    private static final LocalDate FUTURE_EVENTS_THRESHOLD = LocalDate.of(2026, 7, 1);
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
    void runSeedsExpandedDemoCatalogWhenRepositoriesAreEmpty() {
        SeedState state = new SeedState();
        stubRepositories(state);

        demoDataSeeder.run();

        assertEquals(3, state.rolesByName.size());
        assertEquals(1, countUsersByRole(state, "ADMIN"));
        assertTrue(countUsersByRole(state, ORGANIZER_ROLE_NAME) >= 7);
        assertTrue(countUsersByRole(state, USER_ROLE_NAME) >= 25);
        assertTrue(state.organizersByLegalName.size() >= 7);
        assertTrue(state.tracksByName.size() >= 20);
        assertTrue(state.servicesByName.size() >= 12);
        assertTrue(countEventsBefore(state, FUTURE_EVENTS_THRESHOLD) >= 8);
        assertTrue(countEventsFrom(state, FUTURE_EVENTS_THRESHOLD) >= 23);
        assertEquals(state.tracksByName.size(), countTracksWithFutureEvents(state, FUTURE_EVENTS_THRESHOLD));
        assertEquals(countEventsFrom(state, FUTURE_EVENTS_THRESHOLD), countFutureEventsWithBookings(state, FUTURE_EVENTS_THRESHOLD));
        assertTrue(state.eventsByTrackAndDate.values().stream()
                .allMatch(event -> event.getDescription() != null && !event.getDescription().isBlank()));
        assertTrue(state.organizersByLegalName.values().stream().anyMatch(organizer -> Boolean.FALSE.equals(organizer.getEnabled())));
        assertTrue(state.eventBookings.size() >= 71);
        assertTrue(state.eventBookings.stream()
                .allMatch(eventBooking -> USER_ROLE_NAME.equals(eventBooking.getUser().getRole().getRoleName())));
        assertTrue(allPastBookingsAreVisible(state, FUTURE_EVENTS_THRESHOLD));
        assertTrue(futureBookingVisibilityRatio(state, FUTURE_EVENTS_THRESHOLD) >= 0.75d);
        assertTrue(state.eventServices.stream()
                .allMatch(eventService -> eventService.getPrice().compareTo(BigDecimal.ZERO) > 0));
        assertTrue(state.eventBookingServices.stream()
                .allMatch(eventBookingService -> eventBookingService.getEventBooking().getEvent().getId()
                        .equals(eventBookingService.getEventService().getEvent().getId())
                        && eventBookingService.getPriceAtPurchase().compareTo(eventBookingService.getEventService().getPrice()) == 0));
        assertTrue(state.lapTimes.size() >= 50);
        assertTrue(state.lapTimes.stream()
                .anyMatch(lapTime -> "Mazda MX-5 NA 1.8".equals(lapTime.getVehicle())));
        assertTrue(allTracksHaveLapTimesBetween(state, 5, 20));
        assertTrue(allRepeatedUserTrackLapTimesUseDifferentVehicles(state));
        assertTrue(state.messages.size() >= 1);
        assertUnreadMessageWasSeeded(state.messages.stream()
                .filter(message -> "Consulta sobre tandas en Jarama".equals(message.getSubject()))
                .findFirst()
                .orElseThrow());
    }

    @Test
    void runIsIdempotentWhenSeederRunsTwiceAgainstSameState() {
        SeedState state = new SeedState();
        stubRepositories(state);

        demoDataSeeder.run();
        clearAllSaveInvocations();

        demoDataSeeder.run();

        verify(roleRepository, never()).save(any(Role.class));
        verify(userRepository, never()).save(any(User.class));
        verify(organizerRepository, never()).save(any(Organizer.class));
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
        stubRepositoriesUntilOrganizerServices(state);

        when(organizerRepository.findByLegalName("TrackEvents S.L.")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> demoDataSeeder.run());

        assertEquals("Organizer not found in demo seed: TrackEvents S.L.", exception.getMessage());
    }

    private void assertUnreadMessageWasSeeded(Message savedMessage) {
        assertEquals("juanje", savedMessage.getSender().getDisplayName());
        assertEquals("trackevents", savedMessage.getReceiver().getDisplayName());
        assertEquals("Consulta sobre tandas en Jarama", savedMessage.getSubject());
        assertEquals(UNREAD_MESSAGE_CONTENT, savedMessage.getContent());
        assertFalse(savedMessage.getIsRead());
    }

    private int countUsersByRole(SeedState state, String roleName) {
        return Math.toIntExact(state.usersByDisplayName.values().stream()
                .filter(user -> user.getRole() != null && roleName.equals(user.getRole().getRoleName()))
                .count());
    }

    private int countEventsBefore(SeedState state, LocalDate threshold) {
        return Math.toIntExact(state.eventsByTrackAndDate.values().stream()
                .filter(event -> event.getEventDate().isBefore(threshold))
                .count());
    }

    private int countEventsFrom(SeedState state, LocalDate threshold) {
        return Math.toIntExact(state.eventsByTrackAndDate.values().stream()
                .filter(event -> !event.getEventDate().isBefore(threshold))
                .count());
    }

    private int countTracksWithFutureEvents(SeedState state, LocalDate threshold) {
        return Math.toIntExact(state.eventsByTrackAndDate.values().stream()
                .filter(event -> !event.getEventDate().isBefore(threshold))
                .map(event -> event.getTrack().getId())
                .distinct()
                .count());
    }

    private int countFutureEventsWithBookings(SeedState state, LocalDate threshold) {
        return Math.toIntExact(state.eventBookings.stream()
                .map(EventBooking::getEvent)
                .filter(event -> !event.getEventDate().isBefore(threshold))
                .map(Event::getId)
                .distinct()
                .count());
    }

    private boolean allTracksHaveLapTimesBetween(SeedState state, int minimum, int maximum) {
        for (Track track : state.tracksByName.values()) {
            long lapTimesForTrack = state.lapTimes.stream()
                    .filter(lapTime -> lapTime.getTrack().getId().equals(track.getId()))
                    .count();

            if (lapTimesForTrack < minimum || lapTimesForTrack > maximum) {
                return false;
            }
        }

        return true;
    }

    private boolean allPastBookingsAreVisible(SeedState state, LocalDate threshold) {
        return state.eventBookings.stream()
                .filter(eventBooking -> eventBooking.getEvent().getEventDate().isBefore(threshold))
                .allMatch(EventBooking::isVisible);
    }

    private double futureBookingVisibilityRatio(SeedState state, LocalDate threshold) {
        List<EventBooking> futureBookings = state.eventBookings.stream()
                .filter(eventBooking -> !eventBooking.getEvent().getEventDate().isBefore(threshold))
                .toList();

        if (futureBookings.isEmpty()) {
            return 1d;
        }

        long visibleFutureBookings = futureBookings.stream()
                .filter(EventBooking::isVisible)
                .count();

        return (double) visibleFutureBookings / futureBookings.size();
    }

    private boolean allRepeatedUserTrackLapTimesUseDifferentVehicles(SeedState state) {
        for (List<LapTime> lapTimesByUserAndTrack : state.lapTimesByPair.values()) {
            long distinctVehicles = lapTimesByUserAndTrack.stream()
                    .map(LapTime::getVehicle)
                    .distinct()
                    .count();

            if (distinctVehicles != lapTimesByUserAndTrack.size()) {
                return false;
            }
        }

        return true;
    }

    private void clearAllSaveInvocations() {
        clearInvocations(
                roleRepository,
                userRepository,
                organizerRepository,
                trackRepository,
                serviceRepository,
                organizerServiceRepository,
                trackServiceRepository,
                eventRepository,
                eventServiceRepository,
                eventBookingRepository,
                eventBookingServiceRepository,
                lapTimeRepository,
                messageRepository
        );
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
        when(userRepository.findByEmail(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.usersByEmail.get(invocation.getArgument(0))));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(state.nextUserId.getAndIncrement());
            }
            state.usersByDisplayName.put(user.getDisplayName(), user);
            state.usersByEmail.put(user.getEmail(), user);
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
        stubRepositoriesAfterCatalogBootstrapping(state);
    }

    private void stubRepositoriesUntilOrganizerServices(SeedState state) {
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
        when(userRepository.findByEmail(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(state.usersByEmail.get(invocation.getArgument(0))));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(state.nextUserId.getAndIncrement());
            }
            state.usersByDisplayName.put(user.getDisplayName(), user);
            state.usersByEmail.put(user.getEmail(), user);
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
    }

    private void stubRepositoriesAfterCatalogBootstrapping(SeedState state) {

        when(organizerServiceRepository.findByOrganizerIdUserAndServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.organizerServicesByPair.get(state.pairKey(
                        invocation.getArgument(0),
                        invocation.getArgument(1)
                ))));
        when(organizerServiceRepository.save(any(OrganizerService.class))).thenAnswer(invocation -> {
            OrganizerService organizerService = invocation.getArgument(0);
            if (organizerService.getId() == null) {
                organizerService.setId(state.nextOrganizerServiceId.getAndIncrement());
            }
            state.organizerServicesByPair.put(
                    state.pairKey(organizerService.getOrganizer().getIdUser(), organizerService.getService().getId()),
                    organizerService
            );
            return organizerService;
        });

        when(trackServiceRepository.findByTrackIdAndServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.trackServicesByPair.get(state.pairKey(
                        invocation.getArgument(0),
                        invocation.getArgument(1)
                ))));
        when(trackServiceRepository.save(any(TrackService.class))).thenAnswer(invocation -> {
            TrackService trackService = invocation.getArgument(0);
            if (trackService.getId() == null) {
                trackService.setId(state.nextTrackServiceId.getAndIncrement());
            }
            state.trackServicesByPair.put(
                    state.pairKey(trackService.getTrack().getId(), trackService.getService().getId()),
                    trackService
            );
            return trackService;
        });

        when(eventRepository.findByTrackIdAndEventDate(anyLong(), any(LocalDate.class)))
                .thenAnswer(invocation -> Optional.ofNullable(state.eventsByTrackAndDate.get(
                        state.trackDateKey(invocation.getArgument(0), invocation.getArgument(1))
                )));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            if (event.getId() == null) {
                event.setId(state.nextEventId.getAndIncrement());
            }
            state.eventsByTrackAndDate.values().removeIf(existingEvent -> existingEvent.getId().equals(event.getId()));
            state.eventsByTrackAndDate.put(state.trackDateKey(event.getTrack().getId(), event.getEventDate()), event);
            return event;
        });

        when(eventBookingRepository.findByUserIdAndEventId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.eventBookingsByUserAndEvent.get(
                        state.pairKey(invocation.getArgument(0), invocation.getArgument(1))
                )));
        when(eventBookingRepository.save(any(EventBooking.class))).thenAnswer(invocation -> {
            EventBooking eventBooking = invocation.getArgument(0);
            if (eventBooking.getId() == null) {
                eventBooking.setId(state.nextEventBookingId.getAndIncrement());
            }
            state.eventBookingsByUserAndEvent.put(
                    state.pairKey(eventBooking.getUser().getId(), eventBooking.getEvent().getId()),
                    eventBooking
            );
            state.eventBookings.removeIf(existingBooking -> existingBooking.getId().equals(eventBooking.getId()));
            state.eventBookings.add(eventBooking);
            return eventBooking;
        });

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
        when(eventServiceRepository.save(any(EventService.class))).thenAnswer(invocation -> {
            EventService eventService = invocation.getArgument(0);
            if (eventService.getId() == null) {
                eventService.setId(state.nextEventServiceId.getAndIncrement());
            }
            if (eventService.getTrackService() != null) {
                state.eventServicesByEventAndTrackService.put(
                        state.pairKey(eventService.getEvent().getId(), eventService.getTrackService().getId()),
                        eventService
                );
            }
            if (eventService.getOrganizerService() != null) {
                state.eventServicesByEventAndOrganizerService.put(
                        state.pairKey(eventService.getEvent().getId(), eventService.getOrganizerService().getId()),
                        eventService
                );
            }
            state.eventServices.removeIf(existingService -> existingService.getId().equals(eventService.getId()));
            state.eventServices.add(eventService);
            return eventService;
        });

        when(eventBookingServiceRepository.findByEventBookingIdAndEventServiceId(anyLong(), anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(state.eventBookingServicesByBookingAndEventService.get(
                        state.pairKey(invocation.getArgument(0), invocation.getArgument(1))
                )));
        when(eventBookingServiceRepository.save(any(EventBookingService.class))).thenAnswer(invocation -> {
            EventBookingService eventBookingService = invocation.getArgument(0);
            if (eventBookingService.getId() == null) {
                eventBookingService.setId(state.nextEventBookingServiceId.getAndIncrement());
            }
            state.eventBookingServicesByBookingAndEventService.put(
                    state.pairKey(eventBookingService.getEventBooking().getId(), eventBookingService.getEventService().getId()),
                    eventBookingService
            );
            state.eventBookingServices.removeIf(existingBookingService -> existingBookingService.getId().equals(eventBookingService.getId()));
            state.eventBookingServices.add(eventBookingService);
            return eventBookingService;
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
                    ignored -> new ArrayList<>()
            ).add(lapTime);
            state.lapTimes.add(lapTime);
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
                    ignored -> new ArrayList<>()
            ).add(message);
            state.messages.add(message);
            return message;
        });
    }

    private static final class SeedState {
        private final AtomicLong nextUserId = new AtomicLong(1);
        private final AtomicLong nextTrackId = new AtomicLong(1);
        private final AtomicLong nextServiceId = new AtomicLong(1);
        private final AtomicLong nextOrganizerServiceId = new AtomicLong(1);
        private final AtomicLong nextTrackServiceId = new AtomicLong(1);
        private final AtomicLong nextEventId = new AtomicLong(1);
        private final AtomicLong nextEventBookingId = new AtomicLong(1);
        private final AtomicLong nextEventServiceId = new AtomicLong(1);
        private final AtomicLong nextEventBookingServiceId = new AtomicLong(1);
        private final AtomicLong nextLapTimeId = new AtomicLong(1);
        private final AtomicLong nextMessageId = new AtomicLong(1);

        private final Map<String, Role> rolesByName = new HashMap<>();
        private final Map<String, User> usersByDisplayName = new HashMap<>();
        private final Map<String, User> usersByEmail = new HashMap<>();
        private final Map<String, Organizer> organizersByLegalName = new HashMap<>();
        private final Map<String, Track> tracksByName = new HashMap<>();
        private final Map<String, Service> servicesByName = new HashMap<>();
        private final Map<String, OrganizerService> organizerServicesByPair = new HashMap<>();
        private final Map<String, TrackService> trackServicesByPair = new HashMap<>();
        private final Map<String, Event> eventsByTrackAndDate = new HashMap<>();
        private final Map<String, EventBooking> eventBookingsByUserAndEvent = new HashMap<>();
        private final Map<String, EventService> eventServicesByEventAndTrackService = new HashMap<>();
        private final Map<String, EventService> eventServicesByEventAndOrganizerService = new HashMap<>();
        private final Map<String, EventBookingService> eventBookingServicesByBookingAndEventService = new HashMap<>();
        private final Map<String, List<LapTime>> lapTimesByPair = new HashMap<>();
        private final Map<String, List<Message>> conversationsByPair = new HashMap<>();
        private final List<EventBooking> eventBookings = new ArrayList<>();
        private final List<EventService> eventServices = new ArrayList<>();
        private final List<EventBookingService> eventBookingServices = new ArrayList<>();
        private final List<LapTime> lapTimes = new ArrayList<>();
        private final List<Message> messages = new ArrayList<>();

        private String pairKey(Long left, Long right) {
            return left + "#" + right;
        }

        private String trackDateKey(Long trackId, LocalDate eventDate) {
            return trackId + "#" + eventDate;
        }
    }
}
