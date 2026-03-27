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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("demo")
public class DemoDataSeeder implements CommandLineRunner {

    private static final String TRACKEVENTS_LEGAL_NAME = "TrackEvents S.L.";
    private static final String RACINGPRO_LEGAL_NAME = "RacingPro S.L.";
    private static final String IBERIAN_MOTORSPORT_LEGAL_NAME = "Iberian Motorsport Events S.L.";
    private static final String DEFAULT_STANDARD_USER_LOGIN = "user123";
    private static final String DEFAULT_ORGANIZER_LOGIN = "org123";
    private static final String USER_ROLE_NAME = "USER";
    private static final String JUANJE_DISPLAY_NAME = "juanje";
    private static final String MARIA_DISPLAY_NAME = "maria";
    private static final String CARLOS_DISPLAY_NAME = "carlos";
    private static final String FERNANDO_ALONSO_DISPLAY_NAME = "fernando.alonso";
    private static final String ALEX_PALAU_DISPLAY_NAME = "alex.palau";
    private static final String TRACKEVENTS_DISPLAY_NAME = "trackevents";

    private static final String CALAFAT_TRACK_NAME = "Circuit Calafat";
    private static final String JARAMA_TRACK_NAME = "Circuito de Madrid Jarama - RACE";
    private static final String RICARDO_TORMO_TRACK_NAME = "Circuit Ricardo Tormo";
    private static final String GUADIX_TRACK_NAME = "Circuito Mike G Guadix";
    private static final String ALGARVE_TRACK_NAME = "Autodromo Internacional do Algarve";
    private static final String NURBURGRING_TRACK_NAME = "Nurburgring";

    private static final String BOX_RENTAL_SERVICE_NAME = "Alquiler de box";
    private static final String COVERED_PADDOCK_SERVICE_NAME = "Reserva de paddock cubierto";
    private static final String NOISE_CONTROL_SERVICE_NAME = "Control de ruido";
    private static final String SKIDPAD_SERVICE_NAME = "Pista deslizante";
    private static final String EVENT_PHOTOGRAPHY_SERVICE_NAME = "Fotografia del evento";
    private static final String EVENT_VIDEO_SERVICE_NAME = "Video resumen del evento";
    private static final String CATERING_SERVICE_NAME = "Catering para participantes";
    private static final String WELCOME_PACK_SERVICE_NAME = "Welcome pack";
    private static final String INSTRUCTOR_SERVICE_NAME = "Instructor de conduccion";
    private static final String SECOND_DRIVER_INSURANCE_SERVICE_NAME = "Seguro para segundo conductor";
    private static final String COPILOT_INSURANCE_SERVICE_NAME = "Seguro para copiloto";
    private static final String TRANSPONDER_TIMING_SERVICE_NAME = "Cronometraje con transponder";
    private static final String UNREAD_MESSAGE_SUBJECT = "Consulta sobre tandas en Jarama";
    private static final String UNREAD_MESSAGE_CONTENT =
            "Hola, me interesa una tanda en Jarama para abril. \u00BFTen\u00E9is previsto organizar alguna? Gracias.";
    private static final String LEGACY_UNREAD_MESSAGE_CONTENT =
            "Hola, me interesa una tanda en Jarama para abril. ¿Tenéis previsto organizar alguna? Gracias.";

    private static final LocalDate PAST_JARAMA_EVENT_DATE = LocalDate.of(2024, 4, 13);
    private static final LocalDate PAST_CALAFAT_EVENT_DATE = LocalDate.of(2024, 6, 8);
    private static final LocalDate PAST_RICARDO_TORMO_EVENT_DATE = LocalDate.of(2024, 10, 19);
    private static final LocalDate PAST_ALGARVE_EVENT_DATE = LocalDate.of(2025, 2, 22);
    private static final int FUTURE_JARAMA_EVENT_OFFSET_DAYS = 28;
    private static final int FUTURE_CALAFAT_EVENT_OFFSET_DAYS = 49;
    private static final int FUTURE_RICARDO_TORMO_EVENT_OFFSET_DAYS = 77;
    private static final int FUTURE_ALGARVE_EVENT_OFFSET_DAYS = 112;

    private final SpringDataRoleRepository roleRepository;
    private final SpringDataUserRepository userRepository;
    private final SpringDataEventRepository eventRepository;
    private final SpringDataEventBookingRepository eventBookingRepository;
    private final SpringDataEventServiceRepository eventServiceRepository;
    private final SpringDataEventBookingServiceRepository eventBookingServiceRepository;
    private final SpringDataLapTimeRepository lapTimeRepository;
    private final SpringDataMessageRepository messageRepository;
    private final SpringDataOrganizerRepository organizerRepository;
    private final SpringDataOrganizerServiceRepository organizerServiceRepository;
    private final SpringDataTrackRepository trackRepository;
    private final SpringDataTrackServiceRepository trackServiceRepository;
    private final SpringDataServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(SpringDataRoleRepository roleRepository,
                          SpringDataUserRepository userRepository,
                          SpringDataEventRepository eventRepository,
                          SpringDataEventBookingRepository eventBookingRepository,
                          SpringDataEventServiceRepository eventServiceRepository,
                          SpringDataEventBookingServiceRepository eventBookingServiceRepository,
                          SpringDataLapTimeRepository lapTimeRepository,
                          SpringDataMessageRepository messageRepository,
                          SpringDataOrganizerRepository organizerRepository,
                          SpringDataOrganizerServiceRepository organizerServiceRepository,
                          SpringDataTrackRepository trackRepository,
                          SpringDataTrackServiceRepository trackServiceRepository,
                          SpringDataServiceRepository serviceRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventBookingRepository = eventBookingRepository;
        this.eventServiceRepository = eventServiceRepository;
        this.eventBookingServiceRepository = eventBookingServiceRepository;
        this.lapTimeRepository = lapTimeRepository;
        this.messageRepository = messageRepository;
        this.organizerRepository = organizerRepository;
        this.organizerServiceRepository = organizerServiceRepository;
        this.trackRepository = trackRepository;
        this.trackServiceRepository = trackServiceRepository;
        this.serviceRepository = serviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = createRoleIfMissing("ADMIN");
        Role userRole = createRoleIfMissing("USER");
        Role organizerRole = createRoleIfMissing("ORGANIZER");

        createUser("admin", "admin@example.com", "Admin", "Demo", "admin123", adminRole);
        createUser(JUANJE_DISPLAY_NAME, "juanje@example.com", "Juanje", "Demo", DEFAULT_STANDARD_USER_LOGIN, userRole);
        createUser(MARIA_DISPLAY_NAME, "maria@example.com", "Maria", "Demo", DEFAULT_STANDARD_USER_LOGIN, userRole);
        createUser(CARLOS_DISPLAY_NAME, "carlos@example.com", "Carlos", "Demo", DEFAULT_STANDARD_USER_LOGIN, userRole);
        createUser(FERNANDO_ALONSO_DISPLAY_NAME,
                "fernando.alonso@example.com",
                "Fernando",
                "Alonso",
                DEFAULT_STANDARD_USER_LOGIN,
                userRole);
        createUser(ALEX_PALAU_DISPLAY_NAME, "alex.palau@example.com", "Alex", "Palou", DEFAULT_STANDARD_USER_LOGIN, userRole);

        createOrganizer(
                TRACKEVENTS_DISPLAY_NAME,
                "trackevents@example.com",
                "Trackevents",
                "Demo",
                TRACKEVENTS_LEGAL_NAME,
                "B00000001",
                organizerRole
        );
        createOrganizer(
                "racingpro",
                "racingpro@example.com",
                "Racingpro",
                "Demo",
                RACINGPRO_LEGAL_NAME,
                "B00000002",
                organizerRole
        );
        createOrganizer(
                "iberianmotorsport",
                "iberianmotorsport@example.com",
                "Iberian",
                "Motorsport",
                IBERIAN_MOTORSPORT_LEGAL_NAME,
                "B00000003",
                organizerRole
        );

        createTrackIfMissing(
                CALAFAT_TRACK_NAME,
                "L'Ametlla de Mar, Tarragona, Espana",
                "Circuito junto al Mediterraneo, conocido por sus cursos de conduccion y tandas privadas en la costa de Tarragona."
        );
        createTrackIfMissing(
                JARAMA_TRACK_NAME,
                "San Sebastian de los Reyes, Madrid, Espana",
                "Trazado historico del automovilismo espanol, sede habitual de eventos, track days y experiencias de conduccion cerca de Madrid."
        );
        createTrackIfMissing(
                RICARDO_TORMO_TRACK_NAME,
                "Cheste, Valencia, Espana",
                "Circuito permanente de la Comunitat Valenciana, referencia nacional para motociclismo y automovilismo con gradas panoramicas."
        );
        createTrackIfMissing(
                GUADIX_TRACK_NAME,
                "Guadix, Granada, Espana",
                "Circuito andaluz muy usado para tandas, pruebas de desarrollo y entrenamientos, situado en el altiplano granadino."
        );
        createTrackIfMissing(
                ALGARVE_TRACK_NAME,
                "Portimao, Faro, Portugal",
                "Circuito portugues famoso por sus desniveles y curvas ciegas, habitual en competiciones internacionales y pruebas de equipos."
        );
        createTrackIfMissing(
                NURBURGRING_TRACK_NAME,
                "Nurburg, Renania-Palatinado, Alemania",
                "Complejo aleman de referencia mundial, celebre por la Nordschleife y por su importancia historica en el automovilismo europeo."
        );

        createServiceIfMissing(
                BOX_RENTAL_SERVICE_NAME,
                "Reserva de box privado para el evento.",
                true,
                false
        );
        createServiceIfMissing(
                COVERED_PADDOCK_SERVICE_NAME,
                "Uso de plaza en paddock cubierto durante la jornada.",
                true,
                false
        );
        createServiceIfMissing(
                NOISE_CONTROL_SERVICE_NAME,
                "Supervision y medicion del nivel sonoro del vehiculo en pista.",
                true,
                false
        );
        createServiceIfMissing(
                SKIDPAD_SERVICE_NAME,
                "Acceso a ejercicios en superficie deslizante dentro del circuito.",
                true,
                false
        );

        createServiceIfMissing(
                EVENT_PHOTOGRAPHY_SERVICE_NAME,
                "Cobertura fotografica profesional de la jornada.",
                false,
                true
        );
        createServiceIfMissing(
                EVENT_VIDEO_SERVICE_NAME,
                "Edicion de video con los mejores momentos del evento.",
                false,
                true
        );
        createServiceIfMissing(
                CATERING_SERVICE_NAME,
                "Servicio de comida y bebida para asistentes y participantes.",
                false,
                true
        );
        createServiceIfMissing(
                WELCOME_PACK_SERVICE_NAME,
                "Pack de bienvenida con acreditacion y material del evento.",
                false,
                true
        );
        createServiceIfMissing(
                INSTRUCTOR_SERVICE_NAME,
                "Sesion de asesoramiento y acompanamiento con instructor.",
                false,
                true
        );
        createServiceIfMissing(
                SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                "Cobertura adicional para incluir un segundo conductor autorizado.",
                false,
                true
        );
        createServiceIfMissing(
                COPILOT_INSURANCE_SERVICE_NAME,
                "Cobertura adicional para incluir copiloto durante la actividad.",
                false,
                true
        );

        createServiceIfMissing(
                TRANSPONDER_TIMING_SERVICE_NAME,
                "Sistema de cronometraje con transponder para registrar tiempos por vuelta.",
                true,
                true
        );

        seedOrganizerServices();
        seedTrackServices();
        seedPastEvents();
        seedPastEventServices();
        seedPastEventBookings();
        seedPastEventBookingServices();
        seedFutureEvents();
        seedFutureEventServices();
        seedFutureEventBookings();
        seedFutureEventBookingServices();
        seedLapTimes();
        seedMessages();
    }

    private Role createRoleIfMissing(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    return roleRepository.save(role);
                });
    }

    private User createUser(String displayName,
                            String email,
                            String name,
                            String surname,
                            String rawPassword,
                            Role role) {
        User user = new User();
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setName(name);
        user.setSurname(surname);
        user.setAddress("Direccion demo");
        user.setPhone("600000000");
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private void createOrganizer(String displayName,
                                 String email,
                                 String name,
                                 String surname,
                                 String legalName,
                                 String cif,
                                 Role organizerRole) {
        User user = createUser(displayName, email, name, surname, DEFAULT_ORGANIZER_LOGIN, organizerRole);
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found in demo seed: " + displayName));

        Organizer organizer = new Organizer();
        organizer.setIdUser(managedUser.getId());
        organizer.setUser(managedUser);
        organizer.setLegalName(legalName);
        organizer.setCif(cif);
        organizer.setEnabled(true);

        organizerRepository.save(organizer);
    }

    private void createTrackIfMissing(String name, String location, String description) {
        if (trackRepository.findByName(name).isPresent()) {
            return;
        }

        Track track = new Track();
        track.setName(name);
        track.setLocation(location);
        track.setDescription(description);
        trackRepository.save(track);
    }

    private void createServiceIfMissing(String name,
                                        String description,
                                        boolean allowedForTrack,
                                        boolean allowedForOrganizer) {
        if (serviceRepository.findByName(name).isPresent()) {
            return;
        }

        Service service = new Service();
        service.setName(name);
        service.setDescription(description);
        service.setAllowedForTrack(allowedForTrack);
        service.setAllowedForOrganizer(allowedForOrganizer);
        service.setEnabled(true);
        serviceRepository.save(service);
    }

    private void seedOrganizerServices() {
        createOrganizerServiceIfMissing(TRACKEVENTS_LEGAL_NAME, EVENT_PHOTOGRAPHY_SERVICE_NAME);
        createOrganizerServiceIfMissing(TRACKEVENTS_LEGAL_NAME, WELCOME_PACK_SERVICE_NAME);
        createOrganizerServiceIfMissing(TRACKEVENTS_LEGAL_NAME, TRANSPONDER_TIMING_SERVICE_NAME);
        createOrganizerServiceIfMissing(TRACKEVENTS_LEGAL_NAME, SECOND_DRIVER_INSURANCE_SERVICE_NAME);
        createOrganizerServiceIfMissing(TRACKEVENTS_LEGAL_NAME, COPILOT_INSURANCE_SERVICE_NAME);

        createOrganizerServiceIfMissing(RACINGPRO_LEGAL_NAME, EVENT_VIDEO_SERVICE_NAME);
        createOrganizerServiceIfMissing(RACINGPRO_LEGAL_NAME, CATERING_SERVICE_NAME);
        createOrganizerServiceIfMissing(RACINGPRO_LEGAL_NAME, INSTRUCTOR_SERVICE_NAME);
        createOrganizerServiceIfMissing(RACINGPRO_LEGAL_NAME, TRANSPONDER_TIMING_SERVICE_NAME);
        createOrganizerServiceIfMissing(RACINGPRO_LEGAL_NAME, SECOND_DRIVER_INSURANCE_SERVICE_NAME);
        createOrganizerServiceIfMissing(RACINGPRO_LEGAL_NAME, COPILOT_INSURANCE_SERVICE_NAME);

        createOrganizerServiceIfMissing(IBERIAN_MOTORSPORT_LEGAL_NAME, EVENT_PHOTOGRAPHY_SERVICE_NAME);
        createOrganizerServiceIfMissing(IBERIAN_MOTORSPORT_LEGAL_NAME, WELCOME_PACK_SERVICE_NAME);
        createOrganizerServiceIfMissing(IBERIAN_MOTORSPORT_LEGAL_NAME, INSTRUCTOR_SERVICE_NAME);
        createOrganizerServiceIfMissing(IBERIAN_MOTORSPORT_LEGAL_NAME, TRANSPONDER_TIMING_SERVICE_NAME);
        createOrganizerServiceIfMissing(IBERIAN_MOTORSPORT_LEGAL_NAME, SECOND_DRIVER_INSURANCE_SERVICE_NAME);
        createOrganizerServiceIfMissing(IBERIAN_MOTORSPORT_LEGAL_NAME, COPILOT_INSURANCE_SERVICE_NAME);
    }

    private void seedTrackServices() {
        createTrackServiceIfMissing(CALAFAT_TRACK_NAME, BOX_RENTAL_SERVICE_NAME);
        createTrackServiceIfMissing(CALAFAT_TRACK_NAME, NOISE_CONTROL_SERVICE_NAME);
        createTrackServiceIfMissing(CALAFAT_TRACK_NAME, SKIDPAD_SERVICE_NAME);

        createTrackServiceIfMissing(JARAMA_TRACK_NAME, BOX_RENTAL_SERVICE_NAME);
        createTrackServiceIfMissing(JARAMA_TRACK_NAME, COVERED_PADDOCK_SERVICE_NAME);
        createTrackServiceIfMissing(JARAMA_TRACK_NAME, NOISE_CONTROL_SERVICE_NAME);
        createTrackServiceIfMissing(JARAMA_TRACK_NAME, SKIDPAD_SERVICE_NAME);
        createTrackServiceIfMissing(JARAMA_TRACK_NAME, TRANSPONDER_TIMING_SERVICE_NAME);

        createTrackServiceIfMissing(RICARDO_TORMO_TRACK_NAME, BOX_RENTAL_SERVICE_NAME);
        createTrackServiceIfMissing(RICARDO_TORMO_TRACK_NAME, COVERED_PADDOCK_SERVICE_NAME);
        createTrackServiceIfMissing(RICARDO_TORMO_TRACK_NAME, NOISE_CONTROL_SERVICE_NAME);
        createTrackServiceIfMissing(RICARDO_TORMO_TRACK_NAME, TRANSPONDER_TIMING_SERVICE_NAME);

        createTrackServiceIfMissing(GUADIX_TRACK_NAME, BOX_RENTAL_SERVICE_NAME);
        createTrackServiceIfMissing(GUADIX_TRACK_NAME, NOISE_CONTROL_SERVICE_NAME);
        createTrackServiceIfMissing(GUADIX_TRACK_NAME, SKIDPAD_SERVICE_NAME);

        createTrackServiceIfMissing(ALGARVE_TRACK_NAME, BOX_RENTAL_SERVICE_NAME);
        createTrackServiceIfMissing(ALGARVE_TRACK_NAME, COVERED_PADDOCK_SERVICE_NAME);
        createTrackServiceIfMissing(ALGARVE_TRACK_NAME, NOISE_CONTROL_SERVICE_NAME);
        createTrackServiceIfMissing(ALGARVE_TRACK_NAME, TRANSPONDER_TIMING_SERVICE_NAME);

        createTrackServiceIfMissing(NURBURGRING_TRACK_NAME, BOX_RENTAL_SERVICE_NAME);
        createTrackServiceIfMissing(NURBURGRING_TRACK_NAME, COVERED_PADDOCK_SERVICE_NAME);
        createTrackServiceIfMissing(NURBURGRING_TRACK_NAME, NOISE_CONTROL_SERVICE_NAME);
        createTrackServiceIfMissing(NURBURGRING_TRACK_NAME, TRANSPONDER_TIMING_SERVICE_NAME);
    }

    private void seedPastEvents() {
        createEventIfMissing(TRACKEVENTS_LEGAL_NAME, JARAMA_TRACK_NAME, PAST_JARAMA_EVENT_DATE, new BigDecimal("180.00"), 55);
        createEventIfMissing(RACINGPRO_LEGAL_NAME, CALAFAT_TRACK_NAME, PAST_CALAFAT_EVENT_DATE, new BigDecimal("145.00"), 35);
        createEventIfMissing(IBERIAN_MOTORSPORT_LEGAL_NAME,
                RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                new BigDecimal("210.00"),
                70);
        createEventIfMissing(TRACKEVENTS_LEGAL_NAME, ALGARVE_TRACK_NAME, PAST_ALGARVE_EVENT_DATE, new BigDecimal("260.00"), 45);
    }

    private void seedPastEventBookings() {
        createEventBookingIfMissing(JUANJE_DISPLAY_NAME, JARAMA_TRACK_NAME, PAST_JARAMA_EVENT_DATE, LocalDateTime.of(2024, 3, 20, 19, 0));
        createEventBookingIfMissing(MARIA_DISPLAY_NAME, JARAMA_TRACK_NAME, PAST_JARAMA_EVENT_DATE, LocalDateTime.of(2024, 3, 22, 10, 30));
        createEventBookingIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                LocalDateTime.of(2024, 3, 25, 18, 15));

        createEventBookingIfMissing(CARLOS_DISPLAY_NAME, CALAFAT_TRACK_NAME, PAST_CALAFAT_EVENT_DATE, LocalDateTime.of(2024, 5, 14, 20, 0));
        createEventBookingIfMissing(MARIA_DISPLAY_NAME, CALAFAT_TRACK_NAME, PAST_CALAFAT_EVENT_DATE, LocalDateTime.of(2024, 5, 16, 9, 45));

        createEventBookingIfMissing(ALEX_PALAU_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                LocalDateTime.of(2024, 9, 18, 12, 0));
        createEventBookingIfMissing(JUANJE_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                LocalDateTime.of(2024, 9, 21, 19, 30));

        createEventBookingIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                LocalDateTime.of(2025, 1, 23, 18, 40));
        createEventBookingIfMissing(ALEX_PALAU_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                LocalDateTime.of(2025, 1, 25, 10, 10));
        createEventBookingIfMissing(CARLOS_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                LocalDateTime.of(2025, 1, 27, 21, 5));
    }

    private void seedPastEventServices() {
        createTrackEventServiceIfMissing(JARAMA_TRACK_NAME, PAST_JARAMA_EVENT_DATE, BOX_RENTAL_SERVICE_NAME, new BigDecimal("35.00"));
        createTrackEventServiceIfMissing(JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                COVERED_PADDOCK_SERVICE_NAME,
                new BigDecimal("18.00"));
        createTrackEventServiceIfMissing(JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                TRANSPONDER_TIMING_SERVICE_NAME,
                new BigDecimal("15.00"));
        createOrganizerEventServiceIfMissing(JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                EVENT_PHOTOGRAPHY_SERVICE_NAME,
                new BigDecimal("22.00"));
        createOrganizerEventServiceIfMissing(JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                WELCOME_PACK_SERVICE_NAME,
                new BigDecimal("10.00"));
        createOrganizerEventServiceIfMissing(JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                new BigDecimal("28.00"));

        createTrackEventServiceIfMissing(CALAFAT_TRACK_NAME, PAST_CALAFAT_EVENT_DATE, SKIDPAD_SERVICE_NAME, new BigDecimal("25.00"));
        createOrganizerEventServiceIfMissing(CALAFAT_TRACK_NAME,
                PAST_CALAFAT_EVENT_DATE,
                EVENT_VIDEO_SERVICE_NAME,
                new BigDecimal("20.00"));
        createOrganizerEventServiceIfMissing(CALAFAT_TRACK_NAME,
                PAST_CALAFAT_EVENT_DATE,
                INSTRUCTOR_SERVICE_NAME,
                new BigDecimal("45.00"));
        createOrganizerEventServiceIfMissing(CALAFAT_TRACK_NAME,
                PAST_CALAFAT_EVENT_DATE,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                new BigDecimal("24.00"));
        createOrganizerEventServiceIfMissing(CALAFAT_TRACK_NAME,
                PAST_CALAFAT_EVENT_DATE,
                COPILOT_INSURANCE_SERVICE_NAME,
                new BigDecimal("15.00"));

        createTrackEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                BOX_RENTAL_SERVICE_NAME,
                new BigDecimal("40.00"));
        createTrackEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                COVERED_PADDOCK_SERVICE_NAME,
                new BigDecimal("20.00"));
        createTrackEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                TRANSPONDER_TIMING_SERVICE_NAME,
                new BigDecimal("18.00"));
        createOrganizerEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                EVENT_PHOTOGRAPHY_SERVICE_NAME,
                new BigDecimal("25.00"));
        createOrganizerEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                INSTRUCTOR_SERVICE_NAME,
                new BigDecimal("50.00"));
        createOrganizerEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                new BigDecimal("30.00"));

        createTrackEventServiceIfMissing(ALGARVE_TRACK_NAME, PAST_ALGARVE_EVENT_DATE, BOX_RENTAL_SERVICE_NAME, new BigDecimal("45.00"));
        createTrackEventServiceIfMissing(ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                COVERED_PADDOCK_SERVICE_NAME,
                new BigDecimal("25.00"));
        createTrackEventServiceIfMissing(ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                TRANSPONDER_TIMING_SERVICE_NAME,
                new BigDecimal("20.00"));
        createOrganizerEventServiceIfMissing(ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                EVENT_PHOTOGRAPHY_SERVICE_NAME,
                new BigDecimal("24.00"));
        createOrganizerEventServiceIfMissing(ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                WELCOME_PACK_SERVICE_NAME,
                new BigDecimal("12.00"));
        createOrganizerEventServiceIfMissing(ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                COPILOT_INSURANCE_SERVICE_NAME,
                new BigDecimal("18.00"));
    }

    private void seedPastEventBookingServices() {
        createTrackEventBookingServiceIfMissing(JUANJE_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                TRANSPONDER_TIMING_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(JUANJE_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                WELCOME_PACK_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(MARIA_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                EVENT_PHOTOGRAPHY_SERVICE_NAME);
        createTrackEventBookingServiceIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                BOX_RENTAL_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                PAST_JARAMA_EVENT_DATE,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME);

        createTrackEventBookingServiceIfMissing(CARLOS_DISPLAY_NAME,
                CALAFAT_TRACK_NAME,
                PAST_CALAFAT_EVENT_DATE,
                SKIDPAD_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(MARIA_DISPLAY_NAME,
                CALAFAT_TRACK_NAME,
                PAST_CALAFAT_EVENT_DATE,
                INSTRUCTOR_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(MARIA_DISPLAY_NAME,
                CALAFAT_TRACK_NAME,
                PAST_CALAFAT_EVENT_DATE,
                COPILOT_INSURANCE_SERVICE_NAME);

        createTrackEventBookingServiceIfMissing(ALEX_PALAU_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                BOX_RENTAL_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(ALEX_PALAU_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                EVENT_PHOTOGRAPHY_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(JUANJE_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                INSTRUCTOR_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(JUANJE_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                PAST_RICARDO_TORMO_EVENT_DATE,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME);

        createTrackEventBookingServiceIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                BOX_RENTAL_SERVICE_NAME);
        createTrackEventBookingServiceIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                TRANSPONDER_TIMING_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(ALEX_PALAU_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                EVENT_PHOTOGRAPHY_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(CARLOS_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                WELCOME_PACK_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(CARLOS_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                PAST_ALGARVE_EVENT_DATE,
                COPILOT_INSURANCE_SERVICE_NAME);
    }

    private void seedFutureEvents() {
        createEventIfMissing(TRACKEVENTS_LEGAL_NAME,
                JARAMA_TRACK_NAME,
                calculateFutureEventDate(FUTURE_JARAMA_EVENT_OFFSET_DAYS),
                new BigDecimal("205.00"),
                60);
        createEventIfMissing(RACINGPRO_LEGAL_NAME,
                CALAFAT_TRACK_NAME,
                calculateFutureEventDate(FUTURE_CALAFAT_EVENT_OFFSET_DAYS),
                new BigDecimal("155.00"),
                36);
        createEventIfMissing(IBERIAN_MOTORSPORT_LEGAL_NAME,
                RICARDO_TORMO_TRACK_NAME,
                calculateFutureEventDate(FUTURE_RICARDO_TORMO_EVENT_OFFSET_DAYS),
                new BigDecimal("225.00"),
                72);
        createEventIfMissing(TRACKEVENTS_LEGAL_NAME,
                ALGARVE_TRACK_NAME,
                calculateFutureEventDate(FUTURE_ALGARVE_EVENT_OFFSET_DAYS),
                new BigDecimal("285.00"),
                48);
    }

    private void seedFutureEventServices() {
        LocalDate futureJaramaEventDate = calculateFutureEventDate(FUTURE_JARAMA_EVENT_OFFSET_DAYS);
        LocalDate futureCalafatEventDate = calculateFutureEventDate(FUTURE_CALAFAT_EVENT_OFFSET_DAYS);
        LocalDate futureRicardoTormoEventDate = calculateFutureEventDate(FUTURE_RICARDO_TORMO_EVENT_OFFSET_DAYS);
        LocalDate futureAlgarveEventDate = calculateFutureEventDate(FUTURE_ALGARVE_EVENT_OFFSET_DAYS);

        createTrackEventServiceIfMissing(JARAMA_TRACK_NAME, futureJaramaEventDate, BOX_RENTAL_SERVICE_NAME, new BigDecimal("38.00"));
        createTrackEventServiceIfMissing(JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                COVERED_PADDOCK_SERVICE_NAME,
                new BigDecimal("20.00"));
        createTrackEventServiceIfMissing(JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                TRANSPONDER_TIMING_SERVICE_NAME,
                new BigDecimal("16.00"));
        createOrganizerEventServiceIfMissing(JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                EVENT_PHOTOGRAPHY_SERVICE_NAME,
                new BigDecimal("24.00"));
        createOrganizerEventServiceIfMissing(JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                WELCOME_PACK_SERVICE_NAME,
                new BigDecimal("11.00"));
        createOrganizerEventServiceIfMissing(JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                new BigDecimal("30.00"));

        createTrackEventServiceIfMissing(CALAFAT_TRACK_NAME, futureCalafatEventDate, SKIDPAD_SERVICE_NAME, new BigDecimal("27.00"));
        createOrganizerEventServiceIfMissing(CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                EVENT_VIDEO_SERVICE_NAME,
                new BigDecimal("22.00"));
        createOrganizerEventServiceIfMissing(CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                INSTRUCTOR_SERVICE_NAME,
                new BigDecimal("48.00"));
        createOrganizerEventServiceIfMissing(CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                new BigDecimal("25.00"));
        createOrganizerEventServiceIfMissing(CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                COPILOT_INSURANCE_SERVICE_NAME,
                new BigDecimal("16.00"));

        createTrackEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                BOX_RENTAL_SERVICE_NAME,
                new BigDecimal("42.00"));
        createTrackEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                COVERED_PADDOCK_SERVICE_NAME,
                new BigDecimal("22.00"));
        createTrackEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                TRANSPONDER_TIMING_SERVICE_NAME,
                new BigDecimal("19.00"));
        createOrganizerEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                EVENT_PHOTOGRAPHY_SERVICE_NAME,
                new BigDecimal("27.00"));
        createOrganizerEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                INSTRUCTOR_SERVICE_NAME,
                new BigDecimal("52.00"));
        createOrganizerEventServiceIfMissing(RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                new BigDecimal("31.00"));

        createTrackEventServiceIfMissing(ALGARVE_TRACK_NAME, futureAlgarveEventDate, BOX_RENTAL_SERVICE_NAME, new BigDecimal("48.00"));
        createTrackEventServiceIfMissing(ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                COVERED_PADDOCK_SERVICE_NAME,
                new BigDecimal("28.00"));
        createTrackEventServiceIfMissing(ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                TRANSPONDER_TIMING_SERVICE_NAME,
                new BigDecimal("22.00"));
        createOrganizerEventServiceIfMissing(ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                EVENT_PHOTOGRAPHY_SERVICE_NAME,
                new BigDecimal("26.00"));
        createOrganizerEventServiceIfMissing(ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                WELCOME_PACK_SERVICE_NAME,
                new BigDecimal("13.00"));
        createOrganizerEventServiceIfMissing(ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                COPILOT_INSURANCE_SERVICE_NAME,
                new BigDecimal("20.00"));
    }

    private void seedFutureEventBookings() {
        LocalDate futureJaramaEventDate = calculateFutureEventDate(FUTURE_JARAMA_EVENT_OFFSET_DAYS);
        LocalDate futureCalafatEventDate = calculateFutureEventDate(FUTURE_CALAFAT_EVENT_OFFSET_DAYS);
        LocalDate futureRicardoTormoEventDate = calculateFutureEventDate(FUTURE_RICARDO_TORMO_EVENT_OFFSET_DAYS);
        LocalDate futureAlgarveEventDate = calculateFutureEventDate(FUTURE_ALGARVE_EVENT_OFFSET_DAYS);

        createEventBookingIfMissing(JUANJE_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                LocalDateTime.now().minusDays(5));
        createEventBookingIfMissing(MARIA_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                LocalDateTime.now().minusDays(4));
        createEventBookingIfMissing(ALEX_PALAU_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                LocalDateTime.now().minusDays(3));

        createEventBookingIfMissing(CARLOS_DISPLAY_NAME,
                CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                LocalDateTime.now().minusDays(8));
        createEventBookingIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                LocalDateTime.now().minusDays(6));

        createEventBookingIfMissing(ALEX_PALAU_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                LocalDateTime.now().minusDays(10));
        createEventBookingIfMissing(JUANJE_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                LocalDateTime.now().minusDays(9));

        createEventBookingIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                LocalDateTime.now().minusDays(12));
        createEventBookingIfMissing(MARIA_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                LocalDateTime.now().minusDays(11));
        createEventBookingIfMissing(CARLOS_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                LocalDateTime.now().minusDays(7));
    }

    private void seedFutureEventBookingServices() {
        LocalDate futureJaramaEventDate = calculateFutureEventDate(FUTURE_JARAMA_EVENT_OFFSET_DAYS);
        LocalDate futureCalafatEventDate = calculateFutureEventDate(FUTURE_CALAFAT_EVENT_OFFSET_DAYS);
        LocalDate futureRicardoTormoEventDate = calculateFutureEventDate(FUTURE_RICARDO_TORMO_EVENT_OFFSET_DAYS);
        LocalDate futureAlgarveEventDate = calculateFutureEventDate(FUTURE_ALGARVE_EVENT_OFFSET_DAYS);

        createTrackEventBookingServiceIfMissing(JUANJE_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                TRANSPONDER_TIMING_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(JUANJE_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                WELCOME_PACK_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(MARIA_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                EVENT_PHOTOGRAPHY_SERVICE_NAME);
        createTrackEventBookingServiceIfMissing(ALEX_PALAU_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                BOX_RENTAL_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(ALEX_PALAU_DISPLAY_NAME,
                JARAMA_TRACK_NAME,
                futureJaramaEventDate,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME);

        createTrackEventBookingServiceIfMissing(CARLOS_DISPLAY_NAME,
                CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                SKIDPAD_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                INSTRUCTOR_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                CALAFAT_TRACK_NAME,
                futureCalafatEventDate,
                COPILOT_INSURANCE_SERVICE_NAME);

        createTrackEventBookingServiceIfMissing(ALEX_PALAU_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                BOX_RENTAL_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(ALEX_PALAU_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                EVENT_PHOTOGRAPHY_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(JUANJE_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                INSTRUCTOR_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(JUANJE_DISPLAY_NAME,
                RICARDO_TORMO_TRACK_NAME,
                futureRicardoTormoEventDate,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME);

        createTrackEventBookingServiceIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                BOX_RENTAL_SERVICE_NAME);
        createTrackEventBookingServiceIfMissing(FERNANDO_ALONSO_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                TRANSPONDER_TIMING_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(MARIA_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                EVENT_PHOTOGRAPHY_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(CARLOS_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                WELCOME_PACK_SERVICE_NAME);
        createOrganizerEventBookingServiceIfMissing(CARLOS_DISPLAY_NAME,
                ALGARVE_TRACK_NAME,
                futureAlgarveEventDate,
                COPILOT_INSURANCE_SERVICE_NAME);
    }

    private void seedLapTimes() {
        createLapTimeIfMissing(FERNANDO_ALONSO_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 107215L, "Alpine A110 R");
        createLapTimeIfMissing(JUANJE_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 111842L, "BMW M2");
        createLapTimeIfMissing(MARIA_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 118530L, "Toyota GR86");

        createLapTimeIfMissing(ALEX_PALAU_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 102480L, "Porsche 911 GT3");
        createLapTimeIfMissing(CARLOS_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 111965L, "MINI John Cooper Works");

        createLapTimeIfMissing(MARIA_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 95620L, "Hyundai i30 N");
        createLapTimeIfMissing(JUANJE_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 98640L, "Mazda MX-5 NA 1.8");

        createLapTimeIfMissing(FERNANDO_ALONSO_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 101870L, "Alpine A110 R");
        createLapTimeIfMissing(ALEX_PALAU_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 104450L, "CUPRA Leon VZ");

        createLapTimeIfMissing(JUANJE_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 121930L, "Porsche Cayman S");
    }

    private void seedMessages() {
        createMessageIfMissing(
                JUANJE_DISPLAY_NAME,
                TRACKEVENTS_DISPLAY_NAME,
                LocalDateTime.of(2026, 3, 24, 18, 30),
                false,
                UNREAD_MESSAGE_SUBJECT,
                resolveUnreadMessageContent()
        );
    }

    private String resolveUnreadMessageContent() {
        return LEGACY_UNREAD_MESSAGE_CONTENT.contains("\u00C2")
                ? UNREAD_MESSAGE_CONTENT
                : LEGACY_UNREAD_MESSAGE_CONTENT;
    }

    private void createOrganizerServiceIfMissing(String legalName, String serviceName) {
        Organizer organizer = findOrganizerByLegalNameOrThrow(legalName);
        Service service = findServiceByNameOrThrow(serviceName);

        if (organizerServiceRepository.findByOrganizerIdUserAndServiceId(organizer.getIdUser(), service.getId()).isPresent()) {
            return;
        }

        OrganizerService organizerService = new OrganizerService();
        organizerService.setOrganizer(organizer);
        organizerService.setService(service);
        organizerServiceRepository.save(organizerService);
    }

    private void createTrackServiceIfMissing(String trackName, String serviceName) {
        Track track = findTrackByNameOrThrow(trackName);
        Service service = findServiceByNameOrThrow(serviceName);

        if (trackServiceRepository.findByTrackIdAndServiceId(track.getId(), service.getId()).isPresent()) {
            return;
        }

        TrackService trackService = new TrackService();
        trackService.setTrack(track);
        trackService.setService(service);
        trackServiceRepository.save(trackService);
    }

    private void createEventIfMissing(String organizerLegalName,
                                      String trackName,
                                      LocalDate eventDate,
                                      BigDecimal basePrice,
                                      Integer maxParticipants) {
        Organizer organizer = findOrganizerByLegalNameOrThrow(organizerLegalName);
        Track track = findTrackByNameOrThrow(trackName);

        if (eventRepository.findByTrackIdAndEventDate(track.getId(), eventDate).isPresent()) {
            return;
        }

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(eventDate);
        event.setBasePrice(basePrice);
        event.setMaxParticipants(maxParticipants);
        eventRepository.save(event);
    }

    private void createEventBookingIfMissing(String attendeeDisplayName,
                                             String trackName,
                                             LocalDate eventDate,
                                             LocalDateTime bookedAt) {
        User attendee = findEventAttendeeByDisplayNameOrThrow(attendeeDisplayName);
        Event event = findEventByTrackAndDateOrThrow(trackName, eventDate);

        if (eventBookingRepository.findByUserIdAndEventId(attendee.getId(), event.getId()).isPresent()) {
            return;
        }

        EventBooking eventBooking = new EventBooking();
        eventBooking.setUser(attendee);
        eventBooking.setEvent(event);
        eventBooking.setBookedAt(bookedAt);
        eventBooking.setBasePriceAtPurchase(event.getBasePrice());
        eventBookingRepository.save(eventBooking);
    }

    private void createTrackEventServiceIfMissing(String trackName,
                                                  LocalDate eventDate,
                                                  String serviceName,
                                                  BigDecimal price) {
        Event event = findEventByTrackAndDateOrThrow(trackName, eventDate);
        TrackService trackService = findTrackServiceForTrackOrThrow(event.getTrack(), serviceName);

        if (eventServiceRepository.findByEventIdAndTrackServiceId(event.getId(), trackService.getId()).isPresent()) {
            return;
        }

        EventService eventService = new EventService();
        eventService.setEvent(event);
        eventService.setTrackService(trackService);
        eventService.setPrice(price);
        eventServiceRepository.save(eventService);
    }

    private void createOrganizerEventServiceIfMissing(String trackName,
                                                      LocalDate eventDate,
                                                      String serviceName,
                                                      BigDecimal price) {
        Event event = findEventByTrackAndDateOrThrow(trackName, eventDate);
        OrganizerService organizerService = findOrganizerServiceForOrganizerOrThrow(event.getOrganizer(), serviceName);

        if (eventServiceRepository.findByEventIdAndOrganizerServiceId(event.getId(), organizerService.getId()).isPresent()) {
            return;
        }

        EventService eventService = new EventService();
        eventService.setEvent(event);
        eventService.setOrganizerService(organizerService);
        eventService.setPrice(price);
        eventServiceRepository.save(eventService);
    }

    private void createTrackEventBookingServiceIfMissing(String attendeeDisplayName,
                                                         String trackName,
                                                         LocalDate eventDate,
                                                         String serviceName) {
        EventBooking eventBooking = findEventBookingOrThrow(attendeeDisplayName, trackName, eventDate);
        TrackService trackService = findTrackServiceForTrackOrThrow(eventBooking.getEvent().getTrack(), serviceName);
        EventService eventService = eventServiceRepository.findByEventIdAndTrackServiceId(
                        eventBooking.getEvent().getId(),
                        trackService.getId()
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Event service not found in demo seed for track service: "
                                + serviceName
                                + ", track: "
                                + trackName
                                + ", date: "
                                + eventDate
                ));

        if (eventBookingServiceRepository.findByEventBookingIdAndEventServiceId(eventBooking.getId(), eventService.getId()).isPresent()) {
            return;
        }

        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(eventService);
        eventBookingService.setPriceAtPurchase(eventService.getPrice());
        eventBookingServiceRepository.save(eventBookingService);
    }

    private void createOrganizerEventBookingServiceIfMissing(String attendeeDisplayName,
                                                             String trackName,
                                                             LocalDate eventDate,
                                                             String serviceName) {
        EventBooking eventBooking = findEventBookingOrThrow(attendeeDisplayName, trackName, eventDate);
        OrganizerService organizerService = findOrganizerServiceForOrganizerOrThrow(
                eventBooking.getEvent().getOrganizer(),
                serviceName
        );
        EventService eventService = eventServiceRepository.findByEventIdAndOrganizerServiceId(
                        eventBooking.getEvent().getId(),
                        organizerService.getId()
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Event service not found in demo seed for organizer service: "
                                + serviceName
                                + ", track: "
                                + trackName
                                + ", date: "
                                + eventDate
                ));

        if (eventBookingServiceRepository.findByEventBookingIdAndEventServiceId(eventBooking.getId(), eventService.getId()).isPresent()) {
            return;
        }

        EventBookingService eventBookingService = new EventBookingService();
        eventBookingService.setEventBooking(eventBooking);
        eventBookingService.setEventService(eventService);
        eventBookingService.setPriceAtPurchase(eventService.getPrice());
        eventBookingServiceRepository.save(eventBookingService);
    }

    private void createLapTimeIfMissing(String displayName,
                                        String trackName,
                                        LocalDate lapDate,
                                        Long lapTimeMs,
                                        String vehicle) {
        User user = findUserByDisplayNameOrThrow(displayName);
        Track track = findTrackByNameOrThrow(trackName);

        boolean exists = lapTimeRepository.findByUserIdAndTrackId(user.getId(), track.getId())
                .stream()
                .anyMatch(lapTime -> lapDate.equals(lapTime.getLapDate())
                        && lapTimeMs.equals(lapTime.getLapTimeMs())
                        && vehicle.equals(lapTime.getVehicle()));

        if (exists) {
            return;
        }

        LapTime lapTime = new LapTime();
        lapTime.setUser(user);
        lapTime.setTrack(track);
        lapTime.setLapDate(lapDate);
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setVehicle(vehicle);
        lapTimeRepository.save(lapTime);
    }

    private void createMessageIfMissing(String senderDisplayName,
                                        String receiverDisplayName,
                                        LocalDateTime sentAt,
                                        boolean isRead,
                                        String subject,
                                        String content) {
        User sender = findUserByDisplayNameOrThrow(senderDisplayName);
        User receiver = findUserByDisplayNameOrThrow(receiverDisplayName);

        boolean exists = messageRepository.findConversation(sender.getId(), receiver.getId())
                .stream()
                .anyMatch(message -> sender.getId().equals(message.getSender().getId())
                        && receiver.getId().equals(message.getReceiver().getId())
                        && sentAt.equals(message.getSentAt())
                        && subject.equals(message.getSubject())
                        && content.equals(message.getContent()));

        if (exists) {
            return;
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSentAt(sentAt);
        message.setIsRead(isRead);
        message.setSubject(subject);
        message.setContent(content);
        messageRepository.save(message);
    }

    private User findUserByDisplayNameOrThrow(String displayName) {
        return userRepository.findByDisplayName(displayName)
                .orElseThrow(() -> new IllegalStateException("User not found in demo seed: " + displayName));
    }

    private User findEventAttendeeByDisplayNameOrThrow(String displayName) {
        User user = findUserByDisplayNameOrThrow(displayName);

        if (user.getRole() == null || !USER_ROLE_NAME.equals(user.getRole().getRoleName())) {
            throw new IllegalStateException("Demo event attendee must have USER role: " + displayName);
        }

        return user;
    }

    private Organizer findOrganizerByLegalNameOrThrow(String legalName) {
        return organizerRepository.findByLegalName(legalName)
                .orElseThrow(() -> new IllegalStateException("Organizer not found in demo seed: " + legalName));
    }

    private Track findTrackByNameOrThrow(String trackName) {
        return trackRepository.findByName(trackName)
                .orElseThrow(() -> new IllegalStateException("Track not found in demo seed: " + trackName));
    }

    private Event findEventByTrackAndDateOrThrow(String trackName, LocalDate eventDate) {
        Track track = findTrackByNameOrThrow(trackName);

        return eventRepository.findByTrackIdAndEventDate(track.getId(), eventDate)
                .orElseThrow(() -> new IllegalStateException(
                        "Event not found in demo seed for track: " + trackName + " and date: " + eventDate
                ));
    }

    private EventBooking findEventBookingOrThrow(String attendeeDisplayName, String trackName, LocalDate eventDate) {
        User attendee = findEventAttendeeByDisplayNameOrThrow(attendeeDisplayName);
        Event event = findEventByTrackAndDateOrThrow(trackName, eventDate);

        return eventBookingRepository.findByUserIdAndEventId(attendee.getId(), event.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Event booking not found in demo seed for attendee: "
                                + attendeeDisplayName
                                + ", track: "
                                + trackName
                                + ", date: "
                                + eventDate
                ));
    }

    private Service findServiceByNameOrThrow(String serviceName) {
        return serviceRepository.findByName(serviceName)
                .orElseThrow(() -> new IllegalStateException("Service not found in demo seed: " + serviceName));
    }

    private LocalDate calculateFutureEventDate(int offsetDays) {
        return LocalDate.now().plusDays(offsetDays);
    }

    private OrganizerService findOrganizerServiceForOrganizerOrThrow(Organizer organizer, String serviceName) {
        Service service = findServiceByNameOrThrow(serviceName);

        return organizerServiceRepository.findByOrganizerIdUserAndServiceId(organizer.getIdUser(), service.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Organizer service not found in demo seed for organizer: "
                                + organizer.getLegalName()
                                + " and service: "
                                + serviceName
                ));
    }

    private TrackService findTrackServiceForTrackOrThrow(Track track, String serviceName) {
        Service service = findServiceByNameOrThrow(serviceName);

        return trackServiceRepository.findByTrackIdAndServiceId(track.getId(), service.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Track service not found in demo seed for track: "
                                + track.getName()
                                + " and service: "
                                + serviceName
                ));
    }
}
