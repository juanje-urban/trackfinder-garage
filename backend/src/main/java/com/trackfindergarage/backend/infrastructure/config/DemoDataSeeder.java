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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("demo")
@Transactional
public class DemoDataSeeder implements CommandLineRunner {

    private static final String TRACKEVENTS_LEGAL_NAME = "TrackEvents S.L.";
    private static final String RACINGPRO_LEGAL_NAME = "RacingPro S.L.";
    private static final String IBERIAN_MOTORSPORT_LEGAL_NAME = "Iberian Motorsport Events S.L.";
    private static final String TRACKLIMITS_IBERIA_LEGAL_NAME = "TrackLimits Iberia S.L.";
    private static final String APEX_IBERIA_LEGAL_NAME = "Apex Iberia Track Days S.L.";
    private static final String LUSITANIA_RACING_LEGAL_NAME = "Lusitania Racing Experience Lda.";
    private static final String MEDITERRANEAN_MOTORSPORT_LEGAL_NAME = "Mediterranean Motorsport Club S.L.";
    private static final String DEFAULT_STANDARD_USER_LOGIN = "user123";
    private static final String DEFAULT_ORGANIZER_LOGIN = "org123";
    private static final String USER_ROLE_NAME = "USER";
    private static final String JUANJE_DISPLAY_NAME = "juanje";
    private static final String MARIA_DISPLAY_NAME = "maria";
    private static final String CARLOS_DISPLAY_NAME = "carlos";
    private static final String FERNANDO_ALONSO_DISPLAY_NAME = "fernando.alonso";
    private static final String ALEX_PALAU_DISPLAY_NAME = "alex.palau";
    private static final String TRACKEVENTS_DISPLAY_NAME = "trackevents";
    private static final String RACINGPRO_DISPLAY_NAME = "racingpro";
    private static final String IBERIAN_MOTORSPORT_DISPLAY_NAME = "iberianmotorsport";
    private static final String TRACKLIMITS_IBERIA_DISPLAY_NAME = "tracklimits.iberia";
    private static final String APEX_IBERIA_DISPLAY_NAME = "apex.iberia";
    private static final String LUSITANIA_RACING_DISPLAY_NAME = "lusitania.racing";
    private static final String MEDITERRANEAN_MOTORSPORT_DISPLAY_NAME = "mediterranean.motorsport";

    private static final String LATEBRAKER_88_DISPLAY_NAME = "latebraker88";
    private static final String CURVA_PERALTADA_DISPLAY_NAME = "curva_peraltada";
    private static final String APEXHUNTER_DISPLAY_NAME = "apexhunter";
    private static final String PITLANE_JUNKIE_DISPLAY_NAME = "pitlane_junkie";
    private static final String KERB_RIDER_DISPLAY_NAME = "kerb_rider";
    private static final String FLATOUT_MARTA_DISPLAY_NAME = "flatout_marta";
    private static final String HEELTOE_DANI_DISPLAY_NAME = "heeltoe_dani";
    private static final String TRACKRAT_77_DISPLAY_NAME = "trackrat_77";
    private static final String BOXBOX_RAUL_DISPLAY_NAME = "boxbox_raul";
    private static final String REDFLAG_INES_DISPLAY_NAME = "redflag_ines";
    private static final String CHICANE_CHASER_DISPLAY_NAME = "chicane_chaser";
    private static final String FULLTHROTTLE_EVA_DISPLAY_NAME = "fullthrottle_eva";
    private static final String GRIDWALKER_DISPLAY_NAME = "gridwalker";
    private static final String OVERSTEER_MIGUEL_DISPLAY_NAME = "oversteer_miguel";
    private static final String TYRESMOKE_LUCIA_DISPLAY_NAME = "tyresmoke_lucia";
    private static final String CURB_ATTACK_DISPLAY_NAME = "curb_attack";
    private static final String BRAKEPOINT_NORA_DISPLAY_NAME = "brakepoint_nora";
    private static final String PADDOCK_PAULA_DISPLAY_NAME = "paddock_paula";
    private static final String STINTMASTER_DISPLAY_NAME = "stintmaster";
    private static final String APEX_LUSO_DISPLAY_NAME = "apex_luso";

    private static final String CALAFAT_TRACK_NAME = "Circuit Calafat";
    private static final String JARAMA_TRACK_NAME = "Circuito de Madrid Jarama - RACE";
    private static final String RICARDO_TORMO_TRACK_NAME = "Circuit Ricardo Tormo";
    private static final String GUADIX_TRACK_NAME = "Circuito Mike G Guadix";
    private static final String ALGARVE_TRACK_NAME = "Autodromo Internacional do Algarve";
    private static final String NURBURGRING_TRACK_NAME = "Nurburgring";
    private static final String LE_MANS_SARTHE_TRACK_NAME = "Circuit de la Sarthe";
    private static final String LE_MANS_BUGATTI_TRACK_NAME = "Bugatti Circuit";
    private static final String NURBURGRING_GP_TRACK_NAME = "Nurburgring Grand Prix-Strecke";
    private static final String BARCELONA_TRACK_NAME = "Circuit de Barcelona-Catalunya";
    private static final String JEREZ_TRACK_NAME = "Circuito de Jerez - Angel Nieto";
    private static final String MOTORLAND_TRACK_NAME = "MotorLand Aragon";
    private static final String NAVARRA_TRACK_NAME = "Circuito de Navarra";
    private static final String ALBACETE_TRACK_NAME = "Circuito de Albacete";
    private static final String MONTEBLANCO_TRACK_NAME = "Circuito de Monteblanco";
    private static final String CARTAGENA_TRACK_NAME = "Circuito de Cartagena";
    private static final String ESTORIL_TRACK_NAME = "Circuito do Estoril";
    private static final String BRAGA_TRACK_NAME = "Circuito Vasco Sameiro";
    private static final String VILA_REAL_TRACK_NAME = "Circuito de Vila Real";
    private static final String BOAVISTA_TRACK_NAME = "Circuito da Boavista";
    private static final String SPA_TRACK_NAME = "Circuit de Spa-Francorchamps";
    private static final String MUGELLO_TRACK_NAME = "Mugello Circuit";
    private static final String PAUL_RICARD_TRACK_NAME = "Circuit Paul Ricard";

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

    private static final BigDecimal PRICE_18_00 = new BigDecimal("18.00");
    private static final BigDecimal PRICE_20_00 = new BigDecimal("20.00");
    private static final BigDecimal PRICE_22_00 = new BigDecimal("22.00");
    private static final BigDecimal PRICE_24_00 = new BigDecimal("24.00");
    private static final BigDecimal PRICE_225_00 = new BigDecimal("225.00");
    private static final BigDecimal BOX_RENTAL_PRICE = new BigDecimal("35.00");
    private static final BigDecimal COVERED_PADDOCK_PRICE = PRICE_20_00;
    private static final BigDecimal SKIDPAD_PRICE = new BigDecimal("27.00");
    private static final BigDecimal TRANSPONDER_PRICE = PRICE_18_00;
    private static final BigDecimal EVENT_PHOTOGRAPHY_PRICE = PRICE_24_00;
    private static final BigDecimal EVENT_VIDEO_PRICE = PRICE_22_00;
    private static final BigDecimal CATERING_PRICE = PRICE_20_00;
    private static final BigDecimal WELCOME_PACK_PRICE = new BigDecimal("12.00");
    private static final BigDecimal INSTRUCTOR_PRICE = new BigDecimal("48.00");
    private static final BigDecimal SECOND_DRIVER_INSURANCE_PRICE = new BigDecimal("30.00");
    private static final BigDecimal COPILOT_INSURANCE_PRICE = PRICE_18_00;

    private static final LocalDate PAST_JARAMA_EVENT_DATE = LocalDate.of(2024, 4, 13);
    private static final LocalDate PAST_CALAFAT_EVENT_DATE = LocalDate.of(2024, 6, 8);
    private static final LocalDate PAST_RICARDO_TORMO_EVENT_DATE = LocalDate.of(2024, 10, 19);
    private static final LocalDate PAST_ALGARVE_EVENT_DATE = LocalDate.of(2025, 2, 22);
    private static final LocalDate PAST_BARCELONA_EVENT_DATE = LocalDate.of(2025, 5, 17);
    private static final LocalDate PAST_JEREZ_EVENT_DATE = LocalDate.of(2025, 9, 20);
    private static final LocalDate PAST_ESTORIL_EVENT_DATE = LocalDate.of(2025, 11, 15);
    private static final LocalDate PAST_MOTORLAND_EVENT_DATE = LocalDate.of(2026, 3, 21);
    private static final LocalDate FUTURE_JARAMA_EVENT_DATE = LocalDate.of(2026, 7, 12);
    private static final LocalDate FUTURE_CALAFAT_EVENT_DATE = LocalDate.of(2026, 7, 19);
    private static final LocalDate FUTURE_ESTORIL_EVENT_DATE = LocalDate.of(2026, 7, 26);
    private static final LocalDate FUTURE_GUADIX_EVENT_DATE = LocalDate.of(2026, 8, 2);
    private static final LocalDate FUTURE_JEREZ_EVENT_DATE = LocalDate.of(2026, 8, 9);
    private static final LocalDate FUTURE_BUGATTI_EVENT_DATE = LocalDate.of(2026, 8, 16);
    private static final LocalDate FUTURE_MONTEBLANCO_EVENT_DATE = LocalDate.of(2026, 8, 23);
    private static final LocalDate FUTURE_BARCELONA_EVENT_DATE = LocalDate.of(2026, 8, 30);
    private static final LocalDate FUTURE_BRAGA_EVENT_DATE = LocalDate.of(2026, 9, 6);
    private static final LocalDate FUTURE_ALGARVE_EVENT_DATE = LocalDate.of(2026, 9, 13);
    private static final LocalDate FUTURE_VILA_REAL_EVENT_DATE = LocalDate.of(2026, 9, 20);
    private static final LocalDate FUTURE_NURBURGRING_EVENT_DATE = LocalDate.of(2026, 9, 27);
    private static final LocalDate FUTURE_RICARDO_TORMO_EVENT_DATE = LocalDate.of(2026, 10, 4);
    private static final LocalDate FUTURE_SARTHE_EVENT_DATE = LocalDate.of(2026, 10, 11);
    private static final LocalDate FUTURE_NAVARRA_EVENT_DATE = LocalDate.of(2026, 10, 18);
    private static final LocalDate FUTURE_NURBURGRING_GP_EVENT_DATE = LocalDate.of(2026, 10, 25);
    private static final LocalDate FUTURE_MOTORLAND_EVENT_DATE = LocalDate.of(2026, 11, 8);
    private static final LocalDate FUTURE_PAUL_RICARD_EVENT_DATE = LocalDate.of(2026, 11, 15);
    private static final LocalDate FUTURE_CARTAGENA_EVENT_DATE = LocalDate.of(2026, 11, 29);
    private static final LocalDate FUTURE_BOAVISTA_EVENT_DATE = LocalDate.of(2026, 12, 6);
    private static final LocalDate FUTURE_ALBACETE_EVENT_DATE = LocalDate.of(2027, 1, 24);
    private static final LocalDate FUTURE_SPA_EVENT_DATE = LocalDate.of(2027, 2, 14);
    private static final LocalDate FUTURE_MUGELLO_EVENT_DATE = LocalDate.of(2027, 3, 14);
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
        Role userRole = createRoleIfMissing(USER_ROLE_NAME);
        Role organizerRole = createRoleIfMissing("ORGANIZER");

        createUser("admin", "admin@example.com", "Admin", "Demo", "admin123", adminRole);
        seedStandardUsers(userRole);
        seedOrganizers(organizerRole);
        seedTracks();
        seedServices();
        seedOrganizerServices();
        seedTrackServices();
        migrateLegacyFutureEvents();
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
        User existingUser = userRepository.findByDisplayName(displayName)
                .or(() -> userRepository.findByEmail(email))
                .orElse(null);

        if (existingUser != null) {
            return existingUser;
        }

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
        if (organizerRepository.findByLegalName(legalName).isPresent()) {
            return;
        }

        User user = createUser(displayName, email, name, surname, DEFAULT_ORGANIZER_LOGIN, organizerRole);

        Organizer organizer = new Organizer();
        organizer.setUser(user);
        organizer.setLegalName(legalName);
        organizer.setCif(cif);
        organizer.setEnabled(true);

        organizerRepository.save(organizer);
    }

    private void seedStandardUsers(Role userRole) {
        standardUserSeeds().forEach(userSeed -> createUser(
                userSeed.displayName(),
                userSeed.email(),
                userSeed.name(),
                userSeed.surname(),
                DEFAULT_STANDARD_USER_LOGIN,
                userRole
        ));
    }

    private void seedOrganizers(Role organizerRole) {
        organizerSeeds().forEach(organizerSeed -> createOrganizer(
                organizerSeed.displayName(),
                organizerSeed.email(),
                organizerSeed.name(),
                organizerSeed.surname(),
                organizerSeed.legalName(),
                organizerSeed.cif(),
                organizerRole
        ));
    }

    private void seedTracks() {
        trackSeeds().forEach(trackSeed -> createTrackIfMissing(
                trackSeed.name(),
                trackSeed.location(),
                trackSeed.description()
        ));
    }

    private void seedServices() {
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
        organizerSeeds().forEach(organizerSeed -> registerOrganizerServices(
                organizerSeed.legalName(),
                EVENT_PHOTOGRAPHY_SERVICE_NAME,
                EVENT_VIDEO_SERVICE_NAME,
                CATERING_SERVICE_NAME,
                WELCOME_PACK_SERVICE_NAME,
                INSTRUCTOR_SERVICE_NAME,
                SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                COPILOT_INSURANCE_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME
        ));
    }

    private void seedTrackServices() {
        registerTrackServices(CALAFAT_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                SKIDPAD_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(JARAMA_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                SKIDPAD_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(RICARDO_TORMO_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(GUADIX_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                SKIDPAD_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(ALGARVE_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(NURBURGRING_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(LE_MANS_SARTHE_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(LE_MANS_BUGATTI_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(NURBURGRING_GP_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(BARCELONA_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(JEREZ_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(MOTORLAND_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(NAVARRA_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(ALBACETE_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                SKIDPAD_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(MONTEBLANCO_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                SKIDPAD_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(CARTAGENA_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                SKIDPAD_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(ESTORIL_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(BRAGA_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                SKIDPAD_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(VILA_REAL_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(BOAVISTA_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(SPA_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(MUGELLO_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
        registerTrackServices(PAUL_RICARD_TRACK_NAME,
                BOX_RENTAL_SERVICE_NAME,
                COVERED_PADDOCK_SERVICE_NAME,
                NOISE_CONTROL_SERVICE_NAME,
                TRANSPONDER_TIMING_SERVICE_NAME);
    }

    private void migrateLegacyFutureEvents() {
        migrateLegacyFutureEvent(
                TRACKEVENTS_LEGAL_NAME,
                JARAMA_TRACK_NAME,
                calculateFutureEventDate(FUTURE_JARAMA_EVENT_OFFSET_DAYS),
                FUTURE_JARAMA_EVENT_DATE,
                price("205.00"),
                60
        );
        migrateLegacyFutureEvent(
                RACINGPRO_LEGAL_NAME,
                CALAFAT_TRACK_NAME,
                calculateFutureEventDate(FUTURE_CALAFAT_EVENT_OFFSET_DAYS),
                FUTURE_CALAFAT_EVENT_DATE,
                price("160.00"),
                38
        );
        migrateLegacyFutureEvent(
                IBERIAN_MOTORSPORT_LEGAL_NAME,
                RICARDO_TORMO_TRACK_NAME,
                calculateFutureEventDate(FUTURE_RICARDO_TORMO_EVENT_OFFSET_DAYS),
                FUTURE_RICARDO_TORMO_EVENT_DATE,
                PRICE_225_00,
                72
        );
        migrateLegacyFutureEvent(
                TRACKEVENTS_LEGAL_NAME,
                ALGARVE_TRACK_NAME,
                calculateFutureEventDate(FUTURE_ALGARVE_EVENT_OFFSET_DAYS),
                FUTURE_ALGARVE_EVENT_DATE,
                price("285.00"),
                50
        );
    }

    private void seedPastEvents() {
        seedEvents(pastEventSeeds());
    }

    private void seedPastEventBookings() {
        seedEventBookings(pastAttendanceSeeds());
    }

    private void seedPastEventServices() {
        seedEventServices(pastEventSeeds());
    }

    private void seedPastEventBookingServices() {
        seedEventBookingServices(pastAttendanceSeeds());
    }

    private void seedFutureEvents() {
        seedEvents(futureEventSeeds());
    }

    private void seedFutureEventServices() {
        seedEventServices(futureEventSeeds());
    }

    private void seedFutureEventBookings() {
        seedEventBookings(futureAttendanceSeeds());
    }

    private void seedFutureEventBookingServices() {
        seedEventBookingServices(futureAttendanceSeeds());
    }

    private void seedLapTimes() {
        lapTimeSeeds().forEach(lapTimeSeed -> createLapTimeIfMissing(
                lapTimeSeed.displayName(),
                lapTimeSeed.trackName(),
                lapTimeSeed.lapDate(),
                lapTimeSeed.lapTimeMs(),
                lapTimeSeed.vehicle()
        ));
    }

    private void registerOrganizerServices(String legalName, String... serviceNames) {
        for (String serviceName : serviceNames) {
            createOrganizerServiceIfMissing(legalName, serviceName);
        }
    }

    private void registerTrackServices(String trackName, String... serviceNames) {
        for (String serviceName : serviceNames) {
            createTrackServiceIfMissing(trackName, serviceName);
        }
    }

    private void migrateLegacyFutureEvent(String organizerLegalName,
                                          String trackName,
                                          LocalDate legacyEventDate,
                                          LocalDate targetEventDate,
                                          BigDecimal basePrice,
                                          int maxParticipants) {
        Track track = findTrackByNameOrThrow(trackName);

        eventRepository.findByTrackIdAndEventDate(track.getId(), legacyEventDate)
                .ifPresent(legacyEvent -> {
                    if (eventRepository.findByTrackIdAndEventDate(track.getId(), targetEventDate).isPresent()) {
                        return;
                    }

                    legacyEvent.setOrganizer(findOrganizerByLegalNameOrThrow(organizerLegalName));
                    legacyEvent.setEventDate(targetEventDate);
                    legacyEvent.setBasePrice(basePrice);
                    legacyEvent.setMaxParticipants(maxParticipants);
                    eventRepository.save(legacyEvent);
                });
    }

    private void seedEvents(List<EventSeed> eventSeeds) {
        eventSeeds.forEach(eventSeed -> createEventIfMissing(
                eventSeed.organizerLegalName(),
                eventSeed.trackName(),
                eventSeed.eventDate(),
                eventSeed.basePrice(),
                eventSeed.maxParticipants()
        ));
    }

    private void seedEventServices(List<EventSeed> eventSeeds) {
        eventSeeds.forEach(eventSeed -> {
            createTrackEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), BOX_RENTAL_SERVICE_NAME, BOX_RENTAL_PRICE);
            createTrackEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), COVERED_PADDOCK_SERVICE_NAME, COVERED_PADDOCK_PRICE);
            createTrackEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), SKIDPAD_SERVICE_NAME, SKIDPAD_PRICE);
            createTrackEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), TRANSPONDER_TIMING_SERVICE_NAME, TRANSPONDER_PRICE);
            createOrganizerEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), EVENT_PHOTOGRAPHY_SERVICE_NAME, EVENT_PHOTOGRAPHY_PRICE);
            createOrganizerEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), EVENT_VIDEO_SERVICE_NAME, EVENT_VIDEO_PRICE);
            createOrganizerEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), CATERING_SERVICE_NAME, CATERING_PRICE);
            createOrganizerEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), WELCOME_PACK_SERVICE_NAME, WELCOME_PACK_PRICE);
            createOrganizerEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), INSTRUCTOR_SERVICE_NAME, INSTRUCTOR_PRICE);
            createOrganizerEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), SECOND_DRIVER_INSURANCE_SERVICE_NAME, SECOND_DRIVER_INSURANCE_PRICE);
            createOrganizerEventServiceIfSupported(eventSeed.trackName(), eventSeed.eventDate(), COPILOT_INSURANCE_SERVICE_NAME, COPILOT_INSURANCE_PRICE);
        });
    }

    private void createTrackEventServiceIfSupported(String trackName,
                                                    LocalDate eventDate,
                                                    String serviceName,
                                                    BigDecimal price) {
        Track track = findTrackByNameOrThrow(trackName);
        Service service = findServiceByNameOrThrow(serviceName);

        if (trackServiceRepository.findByTrackIdAndServiceId(track.getId(), service.getId()).isEmpty()) {
            return;
        }

        createTrackEventServiceIfMissing(trackName, eventDate, serviceName, price);
    }

    private void createOrganizerEventServiceIfSupported(String trackName,
                                                        LocalDate eventDate,
                                                        String serviceName,
                                                        BigDecimal price) {
        Event event = findEventByTrackAndDateOrThrow(trackName, eventDate);
        Service service = findServiceByNameOrThrow(serviceName);

        if (organizerServiceRepository.findByOrganizerIdUserAndServiceId(event.getOrganizer().getIdUser(), service.getId()).isEmpty()) {
            return;
        }

        createOrganizerEventServiceIfMissing(trackName, eventDate, serviceName, price);
    }

    private void seedEventBookings(List<EventAttendanceSeed> attendanceSeeds) {
        attendanceSeeds.forEach(attendanceSeed -> {
            for (int attendeeIndex = 0; attendeeIndex < attendanceSeed.attendees().size(); attendeeIndex++) {
                createEventBookingIfMissing(
                        attendanceSeed.attendees().get(attendeeIndex),
                        attendanceSeed.trackName(),
                        attendanceSeed.eventDate(),
                        bookingTimestamp(attendanceSeed.eventDate(), attendeeIndex)
                );
            }
        });
    }

    private LocalDateTime bookingTimestamp(LocalDate eventDate, int attendeeIndex) {
        int hour = attendeeIndex % 2 == 0 ? 19 - attendeeIndex : 9 + attendeeIndex;
        int minuteOffset = 5 + (attendeeIndex * 11);
        hour += minuteOffset / 60;
        int minute = minuteOffset % 60;
        return eventDate.minusDays(45L - (attendeeIndex * 5L)).atTime(hour, minute);
    }

    private void seedEventBookingServices(List<EventAttendanceSeed> attendanceSeeds) {
        attendanceSeeds.forEach(this::seedDefaultBookingServices);
    }

    private void seedDefaultBookingServices(EventAttendanceSeed attendanceSeed) {
        List<String> attendees = attendanceSeed.attendees();

        if (!attendees.isEmpty()) {
            createTrackEventBookingServiceIfMissing(
                    attendees.getFirst(),
                    attendanceSeed.trackName(),
                    attendanceSeed.eventDate(),
                    TRANSPONDER_TIMING_SERVICE_NAME
            );
            createOrganizerEventBookingServiceIfMissing(
                    attendees.getFirst(),
                    attendanceSeed.trackName(),
                    attendanceSeed.eventDate(),
                    WELCOME_PACK_SERVICE_NAME
            );
        }

        if (attendees.size() > 1) {
            createOrganizerEventBookingServiceIfMissing(
                    attendees.get(1),
                    attendanceSeed.trackName(),
                    attendanceSeed.eventDate(),
                    EVENT_PHOTOGRAPHY_SERVICE_NAME
            );
            createOrganizerEventBookingServiceIfMissing(
                    attendees.get(1),
                    attendanceSeed.trackName(),
                    attendanceSeed.eventDate(),
                    INSTRUCTOR_SERVICE_NAME
            );
        }

        if (attendees.size() > 2) {
            createTrackEventBookingServiceIfMissing(
                    attendees.get(2),
                    attendanceSeed.trackName(),
                    attendanceSeed.eventDate(),
                    BOX_RENTAL_SERVICE_NAME
            );
            createOrganizerEventBookingServiceIfMissing(
                    attendees.get(2),
                    attendanceSeed.trackName(),
                    attendanceSeed.eventDate(),
                    SECOND_DRIVER_INSURANCE_SERVICE_NAME
            );
        }

        if (attendees.size() > 3) {
            createOrganizerEventBookingServiceIfMissing(
                    attendees.get(3),
                    attendanceSeed.trackName(),
                    attendanceSeed.eventDate(),
                    EVENT_VIDEO_SERVICE_NAME
            );
            createOrganizerEventBookingServiceIfMissing(
                    attendees.get(3),
                    attendanceSeed.trackName(),
                    attendanceSeed.eventDate(),
                    COPILOT_INSURANCE_SERVICE_NAME
            );
        }
    }

    private BigDecimal price(String value) {
        return new BigDecimal(value);
    }

    private List<UserSeed> standardUserSeeds() {
        return List.of(
                new UserSeed(JUANJE_DISPLAY_NAME, "juanje@example.com", "Juanje", "Demo"),
                new UserSeed(MARIA_DISPLAY_NAME, "maria@example.com", "Maria", "Demo"),
                new UserSeed(CARLOS_DISPLAY_NAME, "carlos@example.com", "Carlos", "Demo"),
                new UserSeed(FERNANDO_ALONSO_DISPLAY_NAME, "fernando.alonso@example.com", "Fernando", "Alonso"),
                new UserSeed(ALEX_PALAU_DISPLAY_NAME, "alex.palau@example.com", "Alex", "Palou"),
                new UserSeed(LATEBRAKER_88_DISPLAY_NAME, "laura.sanz@example.com", "Laura", "Sanz"),
                new UserSeed(CURVA_PERALTADA_DISPLAY_NAME, "sergio.rivas@example.com", "Sergio", "Rivas"),
                new UserSeed(APEXHUNTER_DISPLAY_NAME, "diego.mena@example.com", "Diego", "Mena"),
                new UserSeed(PITLANE_JUNKIE_DISPLAY_NAME, "ines.duarte@example.com", "Ines", "Duarte"),
                new UserSeed(KERB_RIDER_DISPLAY_NAME, "marta.nogueira@example.com", "Marta", "Nogueira"),
                new UserSeed(FLATOUT_MARTA_DISPLAY_NAME, "marta.cabrera@example.com", "Marta", "Cabrera"),
                new UserSeed(HEELTOE_DANI_DISPLAY_NAME, "daniel.pardo@example.com", "Daniel", "Pardo"),
                new UserSeed(TRACKRAT_77_DISPLAY_NAME, "raul.vega@example.com", "Raul", "Vega"),
                new UserSeed(BOXBOX_RAUL_DISPLAY_NAME, "raul.ochoa@example.com", "Raul", "Ochoa"),
                new UserSeed(REDFLAG_INES_DISPLAY_NAME, "ines.pastor@example.com", "Ines", "Pastor"),
                new UserSeed(CHICANE_CHASER_DISPLAY_NAME, "pablo.ordonez@example.com", "Pablo", "Ordonez"),
                new UserSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, "eva.silva@example.com", "Eva", "Silva"),
                new UserSeed(GRIDWALKER_DISPLAY_NAME, "hugo.lemos@example.com", "Hugo", "Lemos"),
                new UserSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, "miguel.costa@example.com", "Miguel", "Costa"),
                new UserSeed(TYRESMOKE_LUCIA_DISPLAY_NAME, "lucia.roman@example.com", "Lucia", "Roman"),
                new UserSeed(CURB_ATTACK_DISPLAY_NAME, "adrian.prieto@example.com", "Adrian", "Prieto"),
                new UserSeed(BRAKEPOINT_NORA_DISPLAY_NAME, "nora.campos@example.com", "Nora", "Campos"),
                new UserSeed(PADDOCK_PAULA_DISPLAY_NAME, "paula.freitas@example.com", "Paula", "Freitas"),
                new UserSeed(STINTMASTER_DISPLAY_NAME, "alvaro.nieto@example.com", "Alvaro", "Nieto"),
                new UserSeed(APEX_LUSO_DISPLAY_NAME, "tiago.martins@example.com", "Tiago", "Martins")
        );
    }

    private List<OrganizerSeed> organizerSeeds() {
        return List.of(
                new OrganizerSeed(
                        TRACKEVENTS_DISPLAY_NAME,
                        "trackevents@example.com",
                        "Trackevents",
                        "Demo",
                        TRACKEVENTS_LEGAL_NAME,
                        "B00000001"
                ),
                new OrganizerSeed(
                        RACINGPRO_DISPLAY_NAME,
                        "racingpro@example.com",
                        "Racingpro",
                        "Demo",
                        RACINGPRO_LEGAL_NAME,
                        "B00000002"
                ),
                new OrganizerSeed(
                        IBERIAN_MOTORSPORT_DISPLAY_NAME,
                        "iberianmotorsport@example.com",
                        "Iberian",
                        "Motorsport",
                        IBERIAN_MOTORSPORT_LEGAL_NAME,
                        "B00000003"
                ),
                new OrganizerSeed(
                        TRACKLIMITS_IBERIA_DISPLAY_NAME,
                        "tracklimits.iberia@example.com",
                        "Tracklimits",
                        "Iberia",
                        TRACKLIMITS_IBERIA_LEGAL_NAME,
                        "B00000004"
                ),
                new OrganizerSeed(
                        APEX_IBERIA_DISPLAY_NAME,
                        "apex.iberia@example.com",
                        "Apex",
                        "Iberia",
                        APEX_IBERIA_LEGAL_NAME,
                        "B00000005"
                ),
                new OrganizerSeed(
                        LUSITANIA_RACING_DISPLAY_NAME,
                        "lusitania.racing@example.com",
                        "Lusitania",
                        "Racing",
                        LUSITANIA_RACING_LEGAL_NAME,
                        "PT500000001"
                ),
                new OrganizerSeed(
                        MEDITERRANEAN_MOTORSPORT_DISPLAY_NAME,
                        "mediterranean.motorsport@example.com",
                        "Mediterranean",
                        "Motorsport",
                        MEDITERRANEAN_MOTORSPORT_LEGAL_NAME,
                        "B00000006"
                )
        );
    }

    private List<TrackSeed> trackSeeds() {
        return List.of(
                new TrackSeed(
                        CALAFAT_TRACK_NAME,
                        "L'Ametlla de Mar, Tarragona, Espana",
                        "Circuito junto al Mediterraneo, conocido por sus cursos de conduccion y tandas privadas en la costa de Tarragona."
                ),
                new TrackSeed(
                        JARAMA_TRACK_NAME,
                        "San Sebastian de los Reyes, Madrid, Espana",
                        "Trazado historico del automovilismo espanol, sede habitual de eventos, track days y experiencias de conduccion cerca de Madrid."
                ),
                new TrackSeed(
                        RICARDO_TORMO_TRACK_NAME,
                        "Cheste, Valencia, Espana",
                        "Circuito permanente de la Comunitat Valenciana, referencia nacional para motociclismo y automovilismo con gradas panoramicas."
                ),
                new TrackSeed(
                        GUADIX_TRACK_NAME,
                        "Guadix, Granada, Espana",
                        "Circuito andaluz muy usado para tandas, pruebas de desarrollo y entrenamientos, situado en el altiplano granadino."
                ),
                new TrackSeed(
                        ALGARVE_TRACK_NAME,
                        "Portimao, Faro, Portugal",
                        "Circuito portugues famoso por sus desniveles y curvas ciegas, habitual en competiciones internacionales y pruebas de equipos."
                ),
                new TrackSeed(
                        NURBURGRING_TRACK_NAME,
                        "Nurburg, Renania-Palatinado, Alemania",
                        "Complejo aleman de referencia mundial, celebre por la Nordschleife y por su importancia historica en el automovilismo europeo."
                ),
                new TrackSeed(
                        LE_MANS_SARTHE_TRACK_NAME,
                        "Le Mans, Sarthe, Francia",
                        "Trazado semipermanente celebre por las 24 Horas de Le Mans, mezcla de rectas larguisimas y enlazadas rapidas en el oeste de Francia."
                ),
                new TrackSeed(
                        LE_MANS_BUGATTI_TRACK_NAME,
                        "Le Mans, Sarthe, Francia",
                        "Circuito permanente dentro del complejo de Le Mans, habitual para pruebas, competiciones nacionales y track days tecnicos."
                ),
                new TrackSeed(
                        NURBURGRING_GP_TRACK_NAME,
                        "Nurburg, Renania-Palatinado, Alemania",
                        "Variante de gran premio del complejo de Nurburgring, con instalaciones modernas y un paddock preparado para eventos internacionales."
                ),
                new TrackSeed(
                        BARCELONA_TRACK_NAME,
                        "Montmelo, Barcelona, Espana",
                        "Circuito catalan de referencia internacional, habitual en competiciones de primer nivel y en jornadas privadas de alto ritmo."
                ),
                new TrackSeed(
                        JEREZ_TRACK_NAME,
                        "Jerez de la Frontera, Cadiz, Espana",
                        "Trazado andaluz muy conocido por su fluidez y por acoger programas de tandas, cursos avanzados y pruebas privadas."
                ),
                new TrackSeed(
                        MOTORLAND_TRACK_NAME,
                        "Alcaniz, Teruel, Espana",
                        "Complejo aragones moderno y tecnico, con grandes escapatorias y un paddock preparado para eventos de gran afluencia."
                ),
                new TrackSeed(
                        NAVARRA_TRACK_NAME,
                        "Los Arcos, Navarra, Espana",
                        "Circuito rapido y variado del norte de Espana, popular entre clubes y organizadores que buscan fines de semana completos."
                ),
                new TrackSeed(
                        ALBACETE_TRACK_NAME,
                        "Albacete, Castilla-La Mancha, Espana",
                        "Trazado muy apreciado por su equilibrio entre tecnica y velocidad, habitual para entrenamientos y tandas de aficionados."
                ),
                new TrackSeed(
                        MONTEBLANCO_TRACK_NAME,
                        "La Palma del Condado, Huelva, Espana",
                        "Instalacion moderna del sur peninsular, utilizada para track days, desarrollo de vehiculos y jornadas corporativas."
                ),
                new TrackSeed(
                        CARTAGENA_TRACK_NAME,
                        "Fuente Alamo, Murcia, Espana",
                        "Circuito compacto y tecnico del sureste espanol, ideal para tandas privadas y sesiones con coches ligeros."
                ),
                new TrackSeed(
                        ESTORIL_TRACK_NAME,
                        "Cascais, Lisboa, Portugal",
                        "Circuito historico portugues junto al Atlantico, muy atractivo para eventos ibericos y tandas con ambiente premium."
                ),
                new TrackSeed(
                        BRAGA_TRACK_NAME,
                        "Braga, Norte, Portugal",
                        "Trazado portugues de longitud contenida, perfecto para jornadas de comunidad, formacion y sesiones técnicas."
                ),
                new TrackSeed(
                        VILA_REAL_TRACK_NAME,
                        "Vila Real, Norte, Portugal",
                        "Recorrido urbano portugues de caracter rapido y exigente, plausible para eventos demo y experiencias especiales."
                ),
                new TrackSeed(
                        BOAVISTA_TRACK_NAME,
                        "Porto, Norte, Portugal",
                        "Circuito urbano iconico del entorno de Porto, util como referencia para eventos especiales de exhibicion y comunidad."
                ),
                new TrackSeed(
                        SPA_TRACK_NAME,
                        "Stavelot, Lieja, Belgica",
                        "Uno de los circuitos mas emblematicos de Europa, muy asociado a tandas premium y a experiencias de alto nivel."
                ),
                new TrackSeed(
                        MUGELLO_TRACK_NAME,
                        "Scarperia e San Piero, Toscana, Italia",
                        "Trazado italiano muy apreciado por su desnivel, enlazadas rapidas y ambiente de paddock de primer nivel."
                ),
                new TrackSeed(
                        PAUL_RICARD_TRACK_NAME,
                        "Le Castellet, Provenza-Alpes-Costa Azul, Francia",
                        "Circuito frances moderno y versatil, frecuentemente utilizado para entrenamientos privados y eventos internacionales."
                )
        );
    }

    private List<EventSeed> pastEventSeeds() {
        return List.of(
                new EventSeed(TRACKEVENTS_LEGAL_NAME, JARAMA_TRACK_NAME, PAST_JARAMA_EVENT_DATE, price("180.00"), 55),
                new EventSeed(RACINGPRO_LEGAL_NAME, CALAFAT_TRACK_NAME, PAST_CALAFAT_EVENT_DATE, price("145.00"), 35),
                new EventSeed(IBERIAN_MOTORSPORT_LEGAL_NAME, RICARDO_TORMO_TRACK_NAME, PAST_RICARDO_TORMO_EVENT_DATE, price("210.00"), 70),
                new EventSeed(TRACKEVENTS_LEGAL_NAME, ALGARVE_TRACK_NAME, PAST_ALGARVE_EVENT_DATE, price("260.00"), 45),
                new EventSeed(APEX_IBERIA_LEGAL_NAME, BARCELONA_TRACK_NAME, PAST_BARCELONA_EVENT_DATE, price("245.00"), 68),
                new EventSeed(TRACKLIMITS_IBERIA_LEGAL_NAME, JEREZ_TRACK_NAME, PAST_JEREZ_EVENT_DATE, price("195.00"), 50),
                new EventSeed(LUSITANIA_RACING_LEGAL_NAME, ESTORIL_TRACK_NAME, PAST_ESTORIL_EVENT_DATE, price("215.00"), 48),
                new EventSeed(MEDITERRANEAN_MOTORSPORT_LEGAL_NAME, MOTORLAND_TRACK_NAME, PAST_MOTORLAND_EVENT_DATE, PRICE_225_00, 60)
        );
    }

    private List<EventSeed> futureEventSeeds() {
        return List.of(
                new EventSeed(TRACKEVENTS_LEGAL_NAME, JARAMA_TRACK_NAME, FUTURE_JARAMA_EVENT_DATE, price("205.00"), 60),
                new EventSeed(RACINGPRO_LEGAL_NAME, CALAFAT_TRACK_NAME, FUTURE_CALAFAT_EVENT_DATE, price("160.00"), 38),
                new EventSeed(LUSITANIA_RACING_LEGAL_NAME, ESTORIL_TRACK_NAME, FUTURE_ESTORIL_EVENT_DATE, price("220.00"), 48),
                new EventSeed(RACINGPRO_LEGAL_NAME, GUADIX_TRACK_NAME, FUTURE_GUADIX_EVENT_DATE, price("150.00"), 6),
                new EventSeed(TRACKLIMITS_IBERIA_LEGAL_NAME, JEREZ_TRACK_NAME, FUTURE_JEREZ_EVENT_DATE, price("198.00"), 52),
                new EventSeed(APEX_IBERIA_LEGAL_NAME, LE_MANS_BUGATTI_TRACK_NAME, FUTURE_BUGATTI_EVENT_DATE, price("275.00"), 8),
                new EventSeed(TRACKLIMITS_IBERIA_LEGAL_NAME, MONTEBLANCO_TRACK_NAME, FUTURE_MONTEBLANCO_EVENT_DATE, price("178.00"), 14),
                new EventSeed(APEX_IBERIA_LEGAL_NAME, BARCELONA_TRACK_NAME, FUTURE_BARCELONA_EVENT_DATE, price("255.00"), 70),
                new EventSeed(LUSITANIA_RACING_LEGAL_NAME, BRAGA_TRACK_NAME, FUTURE_BRAGA_EVENT_DATE, price("155.00"), 12),
                new EventSeed(TRACKEVENTS_LEGAL_NAME, ALGARVE_TRACK_NAME, FUTURE_ALGARVE_EVENT_DATE, price("285.00"), 50),
                new EventSeed(LUSITANIA_RACING_LEGAL_NAME, VILA_REAL_TRACK_NAME, FUTURE_VILA_REAL_EVENT_DATE, price("190.00"), 6),
                new EventSeed(IBERIAN_MOTORSPORT_LEGAL_NAME, NURBURGRING_TRACK_NAME, FUTURE_NURBURGRING_EVENT_DATE, price("340.00"), 12),
                new EventSeed(IBERIAN_MOTORSPORT_LEGAL_NAME, RICARDO_TORMO_TRACK_NAME, FUTURE_RICARDO_TORMO_EVENT_DATE, PRICE_225_00, 72),
                new EventSeed(TRACKEVENTS_LEGAL_NAME, LE_MANS_SARTHE_TRACK_NAME, FUTURE_SARTHE_EVENT_DATE, price("295.00"), 16),
                new EventSeed(RACINGPRO_LEGAL_NAME, NAVARRA_TRACK_NAME, FUTURE_NAVARRA_EVENT_DATE, price("185.00"), 44),
                new EventSeed(IBERIAN_MOTORSPORT_LEGAL_NAME, NURBURGRING_GP_TRACK_NAME, FUTURE_NURBURGRING_GP_EVENT_DATE, price("255.00"), 7),
                new EventSeed(MEDITERRANEAN_MOTORSPORT_LEGAL_NAME, MOTORLAND_TRACK_NAME, FUTURE_MOTORLAND_EVENT_DATE, price("235.00"), 62),
                new EventSeed(MEDITERRANEAN_MOTORSPORT_LEGAL_NAME, PAUL_RICARD_TRACK_NAME, FUTURE_PAUL_RICARD_EVENT_DATE, price("265.00"), 15),
                new EventSeed(TRACKLIMITS_IBERIA_LEGAL_NAME, CARTAGENA_TRACK_NAME, FUTURE_CARTAGENA_EVENT_DATE, price("175.00"), 40),
                new EventSeed(LUSITANIA_RACING_LEGAL_NAME, BOAVISTA_TRACK_NAME, FUTURE_BOAVISTA_EVENT_DATE, price("168.00"), 6),
                new EventSeed(APEX_IBERIA_LEGAL_NAME, ALBACETE_TRACK_NAME, FUTURE_ALBACETE_EVENT_DATE, price("165.00"), 36),
                new EventSeed(IBERIAN_MOTORSPORT_LEGAL_NAME, SPA_TRACK_NAME, FUTURE_SPA_EVENT_DATE, price("325.00"), 58),
                new EventSeed(LUSITANIA_RACING_LEGAL_NAME, MUGELLO_TRACK_NAME, FUTURE_MUGELLO_EVENT_DATE, price("310.00"), 56)
        );
    }

    private List<EventAttendanceSeed> pastAttendanceSeeds() {
        return List.of(
                new EventAttendanceSeed(JARAMA_TRACK_NAME, PAST_JARAMA_EVENT_DATE, List.of(
                        JUANJE_DISPLAY_NAME,
                        MARIA_DISPLAY_NAME,
                        HEELTOE_DANI_DISPLAY_NAME,
                        LATEBRAKER_88_DISPLAY_NAME
                )),
                new EventAttendanceSeed(CALAFAT_TRACK_NAME, PAST_CALAFAT_EVENT_DATE, List.of(
                        CARLOS_DISPLAY_NAME,
                        REDFLAG_INES_DISPLAY_NAME,
                        APEXHUNTER_DISPLAY_NAME,
                        PADDOCK_PAULA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(RICARDO_TORMO_TRACK_NAME, PAST_RICARDO_TORMO_EVENT_DATE, List.of(
                        ALEX_PALAU_DISPLAY_NAME,
                        JUANJE_DISPLAY_NAME,
                        OVERSTEER_MIGUEL_DISPLAY_NAME,
                        CURB_ATTACK_DISPLAY_NAME
                )),
                new EventAttendanceSeed(ALGARVE_TRACK_NAME, PAST_ALGARVE_EVENT_DATE, List.of(
                        FERNANDO_ALONSO_DISPLAY_NAME,
                        APEX_LUSO_DISPLAY_NAME,
                        TRACKRAT_77_DISPLAY_NAME,
                        TYRESMOKE_LUCIA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(BARCELONA_TRACK_NAME, PAST_BARCELONA_EVENT_DATE, List.of(
                        LATEBRAKER_88_DISPLAY_NAME,
                        CURVA_PERALTADA_DISPLAY_NAME,
                        FLATOUT_MARTA_DISPLAY_NAME,
                        GRIDWALKER_DISPLAY_NAME
                )),
                new EventAttendanceSeed(JEREZ_TRACK_NAME, PAST_JEREZ_EVENT_DATE, List.of(
                        PITLANE_JUNKIE_DISPLAY_NAME,
                        FULLTHROTTLE_EVA_DISPLAY_NAME,
                        BOXBOX_RAUL_DISPLAY_NAME,
                        BRAKEPOINT_NORA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(ESTORIL_TRACK_NAME, PAST_ESTORIL_EVENT_DATE, List.of(
                        APEX_LUSO_DISPLAY_NAME,
                        OVERSTEER_MIGUEL_DISPLAY_NAME,
                        PADDOCK_PAULA_DISPLAY_NAME,
                        KERB_RIDER_DISPLAY_NAME
                )),
                new EventAttendanceSeed(MOTORLAND_TRACK_NAME, PAST_MOTORLAND_EVENT_DATE, List.of(
                        STINTMASTER_DISPLAY_NAME,
                        CHICANE_CHASER_DISPLAY_NAME,
                        CURB_ATTACK_DISPLAY_NAME,
                        MARIA_DISPLAY_NAME
                ))
        );
    }

    private List<EventAttendanceSeed> futureAttendanceSeeds() {
        return List.of(
                new EventAttendanceSeed(JARAMA_TRACK_NAME, FUTURE_JARAMA_EVENT_DATE, List.of(
                        JUANJE_DISPLAY_NAME,
                        LATEBRAKER_88_DISPLAY_NAME,
                        APEXHUNTER_DISPLAY_NAME
                )),
                new EventAttendanceSeed(CALAFAT_TRACK_NAME, FUTURE_CALAFAT_EVENT_DATE, List.of(
                        CARLOS_DISPLAY_NAME,
                        REDFLAG_INES_DISPLAY_NAME,
                        STINTMASTER_DISPLAY_NAME
                )),
                new EventAttendanceSeed(ESTORIL_TRACK_NAME, FUTURE_ESTORIL_EVENT_DATE, List.of(
                        APEX_LUSO_DISPLAY_NAME,
                        KERB_RIDER_DISPLAY_NAME,
                        OVERSTEER_MIGUEL_DISPLAY_NAME
                )),
                new EventAttendanceSeed(GUADIX_TRACK_NAME, FUTURE_GUADIX_EVENT_DATE, List.of(
                        JUANJE_DISPLAY_NAME,
                        MARIA_DISPLAY_NAME,
                        APEXHUNTER_DISPLAY_NAME,
                        TRACKRAT_77_DISPLAY_NAME
                )),
                new EventAttendanceSeed(JEREZ_TRACK_NAME, FUTURE_JEREZ_EVENT_DATE, List.of(
                        HEELTOE_DANI_DISPLAY_NAME,
                        PITLANE_JUNKIE_DISPLAY_NAME,
                        BRAKEPOINT_NORA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(LE_MANS_BUGATTI_TRACK_NAME, FUTURE_BUGATTI_EVENT_DATE, List.of(
                        ALEX_PALAU_DISPLAY_NAME,
                        FERNANDO_ALONSO_DISPLAY_NAME,
                        BOXBOX_RAUL_DISPLAY_NAME,
                        FULLTHROTTLE_EVA_DISPLAY_NAME,
                        APEX_LUSO_DISPLAY_NAME
                )),
                new EventAttendanceSeed(MONTEBLANCO_TRACK_NAME, FUTURE_MONTEBLANCO_EVENT_DATE, List.of(
                        HEELTOE_DANI_DISPLAY_NAME,
                        PADDOCK_PAULA_DISPLAY_NAME,
                        BRAKEPOINT_NORA_DISPLAY_NAME,
                        KERB_RIDER_DISPLAY_NAME,
                        CURB_ATTACK_DISPLAY_NAME
                )),
                new EventAttendanceSeed(BARCELONA_TRACK_NAME, FUTURE_BARCELONA_EVENT_DATE, List.of(
                        ALEX_PALAU_DISPLAY_NAME,
                        GRIDWALKER_DISPLAY_NAME,
                        TYRESMOKE_LUCIA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(BRAGA_TRACK_NAME, FUTURE_BRAGA_EVENT_DATE, List.of(
                        REDFLAG_INES_DISPLAY_NAME,
                        FULLTHROTTLE_EVA_DISPLAY_NAME,
                        CHICANE_CHASER_DISPLAY_NAME,
                        TYRESMOKE_LUCIA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(ALGARVE_TRACK_NAME, FUTURE_ALGARVE_EVENT_DATE, List.of(
                        FERNANDO_ALONSO_DISPLAY_NAME,
                        FULLTHROTTLE_EVA_DISPLAY_NAME,
                        TRACKRAT_77_DISPLAY_NAME
                )),
                new EventAttendanceSeed(VILA_REAL_TRACK_NAME, FUTURE_VILA_REAL_EVENT_DATE, List.of(
                        APEX_LUSO_DISPLAY_NAME,
                        OVERSTEER_MIGUEL_DISPLAY_NAME,
                        GRIDWALKER_DISPLAY_NAME
                )),
                new EventAttendanceSeed(NURBURGRING_TRACK_NAME, FUTURE_NURBURGRING_EVENT_DATE, List.of(
                        ALEX_PALAU_DISPLAY_NAME,
                        FERNANDO_ALONSO_DISPLAY_NAME,
                        STINTMASTER_DISPLAY_NAME,
                        CURVA_PERALTADA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(RICARDO_TORMO_TRACK_NAME, FUTURE_RICARDO_TORMO_EVENT_DATE, List.of(
                        CARLOS_DISPLAY_NAME,
                        BOXBOX_RAUL_DISPLAY_NAME,
                        PADDOCK_PAULA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(LE_MANS_SARTHE_TRACK_NAME, FUTURE_SARTHE_EVENT_DATE, List.of(
                        JUANJE_DISPLAY_NAME,
                        LATEBRAKER_88_DISPLAY_NAME,
                        APEXHUNTER_DISPLAY_NAME,
                        BOXBOX_RAUL_DISPLAY_NAME,
                        TRACKRAT_77_DISPLAY_NAME,
                        FULLTHROTTLE_EVA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(NAVARRA_TRACK_NAME, FUTURE_NAVARRA_EVENT_DATE, List.of(
                        CHICANE_CHASER_DISPLAY_NAME,
                        CURVA_PERALTADA_DISPLAY_NAME,
                        KERB_RIDER_DISPLAY_NAME
                )),
                new EventAttendanceSeed(NURBURGRING_GP_TRACK_NAME, FUTURE_NURBURGRING_GP_EVENT_DATE, List.of(
                        FERNANDO_ALONSO_DISPLAY_NAME,
                        ALEX_PALAU_DISPLAY_NAME,
                        STINTMASTER_DISPLAY_NAME,
                        APEX_LUSO_DISPLAY_NAME,
                        OVERSTEER_MIGUEL_DISPLAY_NAME
                )),
                new EventAttendanceSeed(MOTORLAND_TRACK_NAME, FUTURE_MOTORLAND_EVENT_DATE, List.of(
                        MARIA_DISPLAY_NAME,
                        STINTMASTER_DISPLAY_NAME,
                        CURB_ATTACK_DISPLAY_NAME
                )),
                new EventAttendanceSeed(PAUL_RICARD_TRACK_NAME, FUTURE_PAUL_RICARD_EVENT_DATE, List.of(
                        PADDOCK_PAULA_DISPLAY_NAME,
                        CARLOS_DISPLAY_NAME,
                        APEXHUNTER_DISPLAY_NAME,
                        BRAKEPOINT_NORA_DISPLAY_NAME,
                        LATEBRAKER_88_DISPLAY_NAME,
                        GRIDWALKER_DISPLAY_NAME
                )),
                new EventAttendanceSeed(CARTAGENA_TRACK_NAME, FUTURE_CARTAGENA_EVENT_DATE, List.of(
                        FLATOUT_MARTA_DISPLAY_NAME,
                        REDFLAG_INES_DISPLAY_NAME,
                        TRACKRAT_77_DISPLAY_NAME
                )),
                new EventAttendanceSeed(BOAVISTA_TRACK_NAME, FUTURE_BOAVISTA_EVENT_DATE, List.of(
                        KERB_RIDER_DISPLAY_NAME,
                        BOXBOX_RAUL_DISPLAY_NAME,
                        FULLTHROTTLE_EVA_DISPLAY_NAME
                )),
                new EventAttendanceSeed(ALBACETE_TRACK_NAME, FUTURE_ALBACETE_EVENT_DATE, List.of(
                        PITLANE_JUNKIE_DISPLAY_NAME,
                        LATEBRAKER_88_DISPLAY_NAME,
                        BOXBOX_RAUL_DISPLAY_NAME
                )),
                new EventAttendanceSeed(SPA_TRACK_NAME, FUTURE_SPA_EVENT_DATE, List.of(
                        ALEX_PALAU_DISPLAY_NAME,
                        FULLTHROTTLE_EVA_DISPLAY_NAME,
                        APEXHUNTER_DISPLAY_NAME
                )),
                new EventAttendanceSeed(MUGELLO_TRACK_NAME, FUTURE_MUGELLO_EVENT_DATE, List.of(
                        FERNANDO_ALONSO_DISPLAY_NAME,
                        APEX_LUSO_DISPLAY_NAME,
                        OVERSTEER_MIGUEL_DISPLAY_NAME
                ))
        );
    }

    private List<LapTimeSeed> lapTimeSeeds() {
        return List.of(
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 107215L, "Alpine A110 R"),
                new LapTimeSeed(JUANJE_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 111842L, "BMW M2"),
                new LapTimeSeed(LATEBRAKER_88_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 114960L, "Toyota GR86"),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 102480L, "Porsche 911 GT3"),
                new LapTimeSeed(CARLOS_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 111965L, "MINI John Cooper Works"),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 115410L, "Renault Megane RS"),
                new LapTimeSeed(MARIA_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 95620L, "Hyundai i30 N"),
                new LapTimeSeed(JUANJE_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 98640L, "Mazda MX-5 NA 1.8"),
                new LapTimeSeed(REDFLAG_INES_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 100280L, "Volkswagen Golf GTI Clubsport"),
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 101870L, "Alpine A110 R"),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 104450L, "CUPRA Leon VZ"),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 109260L, "Honda Civic Type R"),
                new LapTimeSeed(JUANJE_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 121930L, "Porsche Cayman S"),
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 117640L, "Aston Martin Vantage"),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 124850L, "BMW M240i"),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, BARCELONA_TRACK_NAME, LocalDate.of(2026, 3, 22), 109930L, "Porsche 718 Cayman GT4"),
                new LapTimeSeed(CURVA_PERALTADA_DISPLAY_NAME, BARCELONA_TRACK_NAME, LocalDate.of(2026, 3, 22), 118770L, "BMW M3 E46"),
                new LapTimeSeed(GRIDWALKER_DISPLAY_NAME, BARCELONA_TRACK_NAME, LocalDate.of(2026, 3, 22), 121580L, "SEAT Leon Cupra"),
                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, JEREZ_TRACK_NAME, LocalDate.of(2026, 3, 24), 119460L, "Audi TTS"),
                new LapTimeSeed(PITLANE_JUNKIE_DISPLAY_NAME, JEREZ_TRACK_NAME, LocalDate.of(2026, 3, 24), 123820L, "Toyota GR Yaris"),
                new LapTimeSeed(BRAKEPOINT_NORA_DISPLAY_NAME, JEREZ_TRACK_NAME, LocalDate.of(2026, 3, 24), 126910L, "Mazda MX-5 ND"),
                new LapTimeSeed(CURB_ATTACK_DISPLAY_NAME, MOTORLAND_TRACK_NAME, LocalDate.of(2026, 3, 26), 132210L, "BMW M2 Competition"),
                new LapTimeSeed(STINTMASTER_DISPLAY_NAME, MOTORLAND_TRACK_NAME, LocalDate.of(2026, 3, 26), 129740L, "Alpine A110 S"),
                new LapTimeSeed(MARIA_DISPLAY_NAME, MOTORLAND_TRACK_NAME, LocalDate.of(2026, 3, 26), 137380L, "Subaru BRZ"),
                new LapTimeSeed(CHICANE_CHASER_DISPLAY_NAME, NAVARRA_TRACK_NAME, LocalDate.of(2026, 3, 28), 111320L, "Renault Clio RS"),
                new LapTimeSeed(KERB_RIDER_DISPLAY_NAME, NAVARRA_TRACK_NAME, LocalDate.of(2026, 3, 28), 114470L, "Ford Fiesta ST"),
                new LapTimeSeed(CURVA_PERALTADA_DISPLAY_NAME, NAVARRA_TRACK_NAME, LocalDate.of(2026, 3, 28), 116880L, "Toyota GT86"),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, ALBACETE_TRACK_NAME, LocalDate.of(2026, 3, 29), 104530L, "Honda S2000"),
                new LapTimeSeed(LATEBRAKER_88_DISPLAY_NAME, ALBACETE_TRACK_NAME, LocalDate.of(2026, 3, 29), 106920L, "Audi RS3"),
                new LapTimeSeed(PITLANE_JUNKIE_DISPLAY_NAME, ALBACETE_TRACK_NAME, LocalDate.of(2026, 3, 29), 109870L, "Renault Clio Cup"),
                new LapTimeSeed(TRACKRAT_77_DISPLAY_NAME, CARTAGENA_TRACK_NAME, LocalDate.of(2026, 3, 30), 103240L, "Lotus Elise S"),
                new LapTimeSeed(FLATOUT_MARTA_DISPLAY_NAME, CARTAGENA_TRACK_NAME, LocalDate.of(2026, 3, 30), 105910L, "Mini Cooper S"),
                new LapTimeSeed(TYRESMOKE_LUCIA_DISPLAY_NAME, CARTAGENA_TRACK_NAME, LocalDate.of(2026, 3, 30), 108330L, "Toyota GR86"),
                new LapTimeSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, ESTORIL_TRACK_NAME, LocalDate.of(2026, 4, 1), 115760L, "Porsche 911 Carrera S"),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, ESTORIL_TRACK_NAME, LocalDate.of(2026, 4, 1), 118140L, "BMW M4"),
                new LapTimeSeed(KERB_RIDER_DISPLAY_NAME, ESTORIL_TRACK_NAME, LocalDate.of(2026, 4, 1), 121020L, "Hyundai Elantra N"),
                new LapTimeSeed(REDFLAG_INES_DISPLAY_NAME, BRAGA_TRACK_NAME, LocalDate.of(2026, 4, 2), 94980L, "Abarth 595"),
                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, BRAGA_TRACK_NAME, LocalDate.of(2026, 4, 2), 92840L, "Caterham Seven 420R"),
                new LapTimeSeed(CHICANE_CHASER_DISPLAY_NAME, BRAGA_TRACK_NAME, LocalDate.of(2026, 4, 2), 97350L, "Suzuki Swift Sport"),
                new LapTimeSeed(CURB_ATTACK_DISPLAY_NAME, SPA_TRACK_NAME, LocalDate.of(2026, 4, 4), 160520L, "BMW M4 CSL"),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, SPA_TRACK_NAME, LocalDate.of(2026, 4, 4), 151880L, "Porsche 911 GT3"),
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, SPA_TRACK_NAME, LocalDate.of(2026, 4, 4), 149330L, "Aston Martin Vantage GT8"),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, MUGELLO_TRACK_NAME, LocalDate.of(2026, 4, 6), 128910L, "Ferrari 488 GTB"),
                new LapTimeSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, MUGELLO_TRACK_NAME, LocalDate.of(2026, 4, 6), 133420L, "BMW M3 Touring"),
                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, MUGELLO_TRACK_NAME, LocalDate.of(2026, 4, 6), 135110L, "Porsche Cayman GTS"),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, NURBURGRING_TRACK_NAME, LocalDate.of(2026, 4, 8), 456210L, "Porsche 911 GT3 RS"),
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, NURBURGRING_TRACK_NAME, LocalDate.of(2026, 4, 8), 448960L, "Aston Martin Vantage AMR"),
                new LapTimeSeed(STINTMASTER_DISPLAY_NAME, NURBURGRING_TRACK_NAME, LocalDate.of(2026, 4, 8), 487340L, "Honda Civic Type R"),
                new LapTimeSeed(PADDOCK_PAULA_DISPLAY_NAME, PAUL_RICARD_TRACK_NAME, LocalDate.of(2026, 4, 10), 134570L, "Alpine A110 S"),
                new LapTimeSeed(CARLOS_DISPLAY_NAME, PAUL_RICARD_TRACK_NAME, LocalDate.of(2026, 4, 10), 131240L, "BMW M2"),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, PAUL_RICARD_TRACK_NAME, LocalDate.of(2026, 4, 10), 129880L, "Porsche 718 Cayman GT4")
        );
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
                                + formatTrackAndDateDetails(trackName, eventDate)
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
                                + formatTrackAndDateDetails(trackName, eventDate)
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
                                + formatTrackAndDateDetails(trackName, eventDate)
                ));
    }

    private Service findServiceByNameOrThrow(String serviceName) {
        return serviceRepository.findByName(serviceName)
                .orElseThrow(() -> new IllegalStateException("Service not found in demo seed: " + serviceName));
    }

    private LocalDate calculateFutureEventDate(int offsetDays) {
        return LocalDate.now().plusDays(offsetDays);
    }

    private String formatTrackAndDateDetails(String trackName, LocalDate eventDate) {
        return ", track: " + trackName + ", date: " + eventDate;
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

    private record UserSeed(String displayName, String email, String name, String surname) {
    }

    private record OrganizerSeed(String displayName,
                                 String email,
                                 String name,
                                 String surname,
                                 String legalName,
                                 String cif) {
    }

    private record TrackSeed(String name, String location, String description) {
    }

    private record EventSeed(String organizerLegalName,
                             String trackName,
                             LocalDate eventDate,
                             BigDecimal basePrice,
                             int maxParticipants) {
    }

    private record EventAttendanceSeed(String trackName, LocalDate eventDate, List<String> attendees) {
    }

    private record LapTimeSeed(String displayName,
                               String trackName,
                               LocalDate lapDate,
                               long lapTimeMs,
                               String vehicle) {
    }
}
