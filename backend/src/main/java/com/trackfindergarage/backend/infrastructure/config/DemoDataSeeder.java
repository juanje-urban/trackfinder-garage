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

import java.math.BigInteger;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private static final String NORDIC_APEX_LEGAL_NAME = "Nordic Apex Track Days S.L.";
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
    private static final String NORDIC_APEX_DISPLAY_NAME = "nordic.apex";

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
    private static final List<String> PRIVATE_PROFILE_ATTENDEE_DISPLAY_NAMES = List.of(
            BOXBOX_RAUL_DISPLAY_NAME,
            GRIDWALKER_DISPLAY_NAME,
            KERB_RIDER_DISPLAY_NAME,
            TRACKRAT_77_DISPLAY_NAME
    );

    private static final String CALAFAT_TRACK_NAME = "Circuit Calafat";
    private static final String JARAMA_TRACK_NAME = "Circuito de Madrid Jarama - RACE";
    private static final String RICARDO_TORMO_TRACK_NAME = "Circuit Ricardo Tormo";
    private static final String GUADIX_TRACK_NAME = "Circuito Mike G Guadix";
    private static final String ALGARVE_TRACK_NAME = "Autódromo Internacional do Algarve";
    private static final String NURBURGRING_TRACK_NAME = "Nürburgring";
    private static final String LE_MANS_SARTHE_TRACK_NAME = "Circuit de la Sarthe";
    private static final String LE_MANS_BUGATTI_TRACK_NAME = "Bugatti Circuit";
    private static final String NURBURGRING_GP_TRACK_NAME = "Nürburgring Grand Prix-Strecke";
    private static final String BARCELONA_TRACK_NAME = "Circuit de Barcelona-Catalunya";
    private static final String JEREZ_TRACK_NAME = "Circuito de Jerez - Ángel Nieto";
    private static final String MOTORLAND_TRACK_NAME = "MotorLand Aragón";
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
    private static final String ALPINE_A110_R_VEHICLE = "Alpine A110 R";
    private static final String BMW_M2_VEHICLE = "BMW M2";
    private static final String TOYOTA_GR86_VEHICLE = "Toyota GR86";
    private static final String HYUNDAI_I30_N_VEHICLE = "Hyundai i30 N";
    private static final String PORSCHE_911_GT3_VEHICLE = "Porsche 911 GT3";
    private static final String MINI_JOHN_COOPER_WORKS_VEHICLE = "MINI John Cooper Works";
    private static final String TOYOTA_GR_YARIS_VEHICLE = "Toyota GR Yaris";
    private static final String ALPINE_A110_VEHICLE = "Alpine A110";
    private static final String ALPINE_A110_S_VEHICLE = "Alpine A110 S";
    private static final String HONDA_CIVIC_TYPE_R_VEHICLE = "Honda Civic Type R";
    private static final String MAZDA_MX5_ND_VEHICLE = "Mazda MX-5 ND";
    private static final String PORSCHE_CAYMAN_GTS_VEHICLE = "Porsche Cayman GTS";

    private static final String BOX_RENTAL_SERVICE_NAME = "Alquiler de box";
    private static final String COVERED_PADDOCK_SERVICE_NAME = "Reserva de paddock cubierto";
    private static final String NOISE_CONTROL_SERVICE_NAME = "Control de ruido";
    private static final String SKIDPAD_SERVICE_NAME = "Pista deslizante";
    private static final String EVENT_PHOTOGRAPHY_SERVICE_NAME = "Fotografía del evento";
    private static final String EVENT_VIDEO_SERVICE_NAME = "Vídeo resumen del evento";
    private static final String CATERING_SERVICE_NAME = "Catering";
    private static final String WELCOME_PACK_SERVICE_NAME = "Welcome pack";
    private static final String INSTRUCTOR_SERVICE_NAME = "Instructor de conducción";
    private static final String SECOND_DRIVER_INSURANCE_SERVICE_NAME = "Seguro para segundo conductor";
    private static final String COPILOT_INSURANCE_SERVICE_NAME = "Seguro para copiloto";
    private static final String TRANSPONDER_TIMING_SERVICE_NAME = "Cronometraje con transponder";
    private static final String UNREAD_MESSAGE_SUBJECT = "Consulta sobre tandas en Jarama";
    private static final String UNREAD_MESSAGE_CONTENT =
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
        syncEventDescriptions(pastEventSeeds());
        seedPastEventServices();
        seedPastEventBookings();
        seedPastEventBookingServices();
        seedFutureEvents();
        syncEventDescriptions(futureEventSeeds());
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
        user.setAddress("Dirección demo");
        user.setPhone(demoPhoneNumber(email));
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setCreated(LocalDateTime.now());
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private String demoPhoneNumber(String uniqueKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(uniqueKey.getBytes(StandardCharsets.UTF_8));
            String digits = new BigInteger(1, digest).toString();
            return "6" + digits.substring(0, 11);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available for demo phone generation", exception);
        }
    }

    private void createOrganizer(OrganizerSeed organizerSeed, Role organizerRole) {
        Organizer existingOrganizer = organizerRepository.findByLegalName(organizerSeed.legalName()).orElse(null);
        if (existingOrganizer != null) {
            if (!Objects.equals(existingOrganizer.getEnabled(), organizerSeed.enabled())) {
                existingOrganizer.setEnabled(organizerSeed.enabled());
                organizerRepository.save(existingOrganizer);
            }
            return;
        }

        User user = createUser(
                organizerSeed.displayName(),
                organizerSeed.email(),
                organizerSeed.name(),
                organizerSeed.surname(),
                DEFAULT_ORGANIZER_LOGIN,
                organizerRole
        );

        Organizer organizer = new Organizer();
        organizer.setUser(user);
        organizer.setLegalName(organizerSeed.legalName());
        organizer.setCif(organizerSeed.cif());
        organizer.setEnabled(organizerSeed.enabled());

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
        organizerSeeds().forEach(organizerSeed -> createOrganizer(organizerSeed, organizerRole));
    }

    private void seedTracks() {
        trackSeeds().forEach(trackSeed -> createTrackIfMissing(
                trackSeed.name(),
                trackSeed.shortName(),
                trackSeed.location(),
                trackSeed.description()
        ));
    }

    private void seedServices() {
        serviceSeeds().forEach(this::createServiceIfMissing);
    }

    private void createTrackIfMissing(String name, String shortName, String location, String description) {
        Optional<Track> existingTrackOptional = trackRepository.findByName(name);
        if (existingTrackOptional.isPresent()) {
            Track existingTrack = existingTrackOptional.get();
            boolean requiresUpdate = false;

            if (!shortName.equals(existingTrack.getShortName())) {
                existingTrack.setShortName(shortName);
                requiresUpdate = true;
            }
            if (!location.equals(existingTrack.getLocation())) {
                existingTrack.setLocation(location);
                requiresUpdate = true;
            }
            if (!description.equals(existingTrack.getDescription())) {
                existingTrack.setDescription(description);
                requiresUpdate = true;
            }

            if (requiresUpdate) {
                trackRepository.save(existingTrack);
            }
            return;
        }

        Track track = new Track();
        track.setName(name);
        track.setShortName(shortName);
        track.setLocation(location);
        track.setDescription(description);
        trackRepository.save(track);
    }

    private void createServiceIfMissing(ServiceSeed serviceSeed) {
        if (serviceRepository.findByName(serviceSeed.name()).isPresent()) {
            return;
        }

        Service service = new Service();
        service.setName(serviceSeed.name());
        service.setDescription(serviceSeed.description());
        service.setAllowedForTrack(serviceSeed.allowedForTrack());
        service.setAllowedForOrganizer(serviceSeed.allowedForOrganizer());
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
                eventSeed.maxParticipants(),
                eventSeed.description()
        ));
    }

    private void syncEventDescriptions(List<EventSeed> eventSeeds) {
        eventSeeds.forEach(eventSeed -> {
            Event event = findEventByTrackAndDateOrThrow(eventSeed.trackName(), eventSeed.eventDate());
            String normalizedDescription = eventSeed.description().trim();

            if (!Objects.equals(event.getDescription(), normalizedDescription)) {
                event.setDescription(normalizedDescription);
                eventRepository.save(event);
            }
        });
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
                new UserSeed(MARIA_DISPLAY_NAME, "maria@example.com", "María", "Demo"),
                new UserSeed(CARLOS_DISPLAY_NAME, "carlos@example.com", "Carlos", "Demo"),
                new UserSeed(FERNANDO_ALONSO_DISPLAY_NAME, "fernando.alonso@example.com", "Fernando", "Alonso"),
                new UserSeed(ALEX_PALAU_DISPLAY_NAME, "alex.palau@example.com", "Álex", "Palou"),
                new UserSeed(LATEBRAKER_88_DISPLAY_NAME, "laura.sanz@example.com", "Laura", "Sanz"),
                new UserSeed(CURVA_PERALTADA_DISPLAY_NAME, "sergio.rivas@example.com", "Sergio", "Rivas"),
                new UserSeed(APEXHUNTER_DISPLAY_NAME, "diego.mena@example.com", "Diego", "Mena"),
                new UserSeed(PITLANE_JUNKIE_DISPLAY_NAME, "ines.duarte@example.com", "Inés", "Duarte"),
                new UserSeed(KERB_RIDER_DISPLAY_NAME, "marta.nogueira@example.com", "Marta", "Nogueira"),
                new UserSeed(FLATOUT_MARTA_DISPLAY_NAME, "marta.cabrera@example.com", "Marta", "Cabrera"),
                new UserSeed(HEELTOE_DANI_DISPLAY_NAME, "daniel.pardo@example.com", "Daniel", "Pardo"),
                new UserSeed(TRACKRAT_77_DISPLAY_NAME, "raul.vega@example.com", "Raúl", "Vega"),
                new UserSeed(BOXBOX_RAUL_DISPLAY_NAME, "raul.ochoa@example.com", "Raúl", "Ochoa"),
                new UserSeed(REDFLAG_INES_DISPLAY_NAME, "ines.pastor@example.com", "Inés", "Pastor"),
                new UserSeed(CHICANE_CHASER_DISPLAY_NAME, "pablo.ordonez@example.com", "Pablo", "Ordóñez"),
                new UserSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, "eva.silva@example.com", "Eva", "Silva"),
                new UserSeed(GRIDWALKER_DISPLAY_NAME, "hugo.lemos@example.com", "Hugo", "Lemos"),
                new UserSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, "miguel.costa@example.com", "Miguel", "Costa"),
                new UserSeed(TYRESMOKE_LUCIA_DISPLAY_NAME, "lucia.roman@example.com", "Lucía", "Román"),
                new UserSeed(CURB_ATTACK_DISPLAY_NAME, "adrian.prieto@example.com", "Adrián", "Prieto"),
                new UserSeed(BRAKEPOINT_NORA_DISPLAY_NAME, "nora.campos@example.com", "Nora", "Campos"),
                new UserSeed(PADDOCK_PAULA_DISPLAY_NAME, "paula.freitas@example.com", "Paula", "Freitas"),
                new UserSeed(STINTMASTER_DISPLAY_NAME, "alvaro.nieto@example.com", "Álvaro", "Nieto"),
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
                        "B00000001",
                        true
                ),
                new OrganizerSeed(
                        RACINGPRO_DISPLAY_NAME,
                        "racingpro@example.com",
                        "Racingpro",
                        "Demo",
                        RACINGPRO_LEGAL_NAME,
                        "B00000002",
                        true
                ),
                new OrganizerSeed(
                        IBERIAN_MOTORSPORT_DISPLAY_NAME,
                        "iberianmotorsport@example.com",
                        "Iberian",
                        "Motorsport",
                        IBERIAN_MOTORSPORT_LEGAL_NAME,
                        "B00000003",
                        true
                ),
                new OrganizerSeed(
                        TRACKLIMITS_IBERIA_DISPLAY_NAME,
                        "tracklimits.iberia@example.com",
                        "Tracklimits",
                        "Iberia",
                        TRACKLIMITS_IBERIA_LEGAL_NAME,
                        "B00000004",
                        true
                ),
                new OrganizerSeed(
                        APEX_IBERIA_DISPLAY_NAME,
                        "apex.iberia@example.com",
                        "Apex",
                        "Iberia",
                        APEX_IBERIA_LEGAL_NAME,
                        "B00000005",
                        true
                ),
                new OrganizerSeed(
                        LUSITANIA_RACING_DISPLAY_NAME,
                        "lusitania.racing@example.com",
                        "Lusitania",
                        "Racing",
                        LUSITANIA_RACING_LEGAL_NAME,
                        "PT500000001",
                        true
                ),
                new OrganizerSeed(
                        MEDITERRANEAN_MOTORSPORT_DISPLAY_NAME,
                        "mediterranean.motorsport@example.com",
                        "Mediterranean",
                        "Motorsport",
                        MEDITERRANEAN_MOTORSPORT_LEGAL_NAME,
                        "B00000006",
                        true
                ),
                new OrganizerSeed(
                        NORDIC_APEX_DISPLAY_NAME,
                        "nordic.apex@example.com",
                        "Nordic",
                        "Apex",
                        NORDIC_APEX_LEGAL_NAME,
                        "B00000007",
                        false
                )
        );
    }

    private List<TrackSeed> trackSeeds() {
        return List.of(
                new TrackSeed(
                        CALAFAT_TRACK_NAME,
                        "calafat",
                        "L'Ametlla de Mar, Tarragona, España",
                        "Circuito junto al Mediterráneo, conocido por sus cursos de conducción y tandas privadas en la costa de Tarragona."
                ),
                new TrackSeed(
                        JARAMA_TRACK_NAME,
                        "jarama",
                        "San Sebastián de los Reyes, Madrid, España",
                        "Trazado histórico del automovilismo español, sede habitual de eventos, track days y experiencias de conducción cerca de Madrid."
                ),
                new TrackSeed(
                        RICARDO_TORMO_TRACK_NAME,
                        "ricardo_tormo",
                        "Cheste, Valencia, España",
                        "Circuito permanente de la Comunitat Valenciana, referencia nacional para motociclismo y automovilismo con gradas panorámicas."
                ),
                new TrackSeed(
                        GUADIX_TRACK_NAME,
                        "guadix",
                        "Guadix, Granada, España",
                        "Circuito andaluz muy usado para tandas, pruebas de desarrollo y entrenamientos, situado en el altiplano granadino."
                ),
                new TrackSeed(
                        ALGARVE_TRACK_NAME,
                        "algarve",
                        "Portimão, Faro, Portugal",
                        "Circuito portugués famoso por sus desniveles y curvas ciegas, habitual en competiciones internacionales y pruebas de equipos."
                ),
                new TrackSeed(
                        NURBURGRING_TRACK_NAME,
                        "nurburgring",
                        "Nürburg, Renania-Palatinado, Alemania",
                        "Complejo alemán de referencia mundial, célebre por la Nordschleife y por su importancia histórica en el automovilismo europeo."
                ),
                new TrackSeed(
                        LE_MANS_SARTHE_TRACK_NAME,
                        "le_mans_sarthe",
                        "Le Mans, Sarthe, Francia",
                        "Trazado semipermanente célebre por las 24 Horas de Le Mans, mezcla de rectas larguísimas y enlazadas rápidas en el oeste de Francia."
                ),
                new TrackSeed(
                        LE_MANS_BUGATTI_TRACK_NAME,
                        "le_mans_bugatti",
                        "Le Mans, Sarthe, Francia",
                        "Circuito permanente dentro del complejo de Le Mans, habitual para pruebas, competiciones nacionales y track days técnicos."
                ),
                new TrackSeed(
                        NURBURGRING_GP_TRACK_NAME,
                        "nurburgring_gp",
                        "Nürburg, Renania-Palatinado, Alemania",
                        "Variante de gran premio del complejo de Nürburgring, con instalaciones modernas y un paddock preparado para eventos internacionales."
                ),
                new TrackSeed(
                        BARCELONA_TRACK_NAME,
                        "barcelona",
                        "Montmeló, Barcelona, España",
                        "Circuito catalán de referencia internacional, habitual en competiciones de primer nivel y en jornadas privadas de alto ritmo."
                ),
                new TrackSeed(
                        JEREZ_TRACK_NAME,
                        "jerez",
                        "Jerez de la Frontera, Cádiz, España",
                        "Trazado andaluz muy conocido por su fluidez y por acoger programas de tandas, cursos avanzados y pruebas privadas."
                ),
                new TrackSeed(
                        MOTORLAND_TRACK_NAME,
                        "motorland",
                        "Alcañiz, Teruel, España",
                        "Complejo aragonés moderno y técnico, con grandes escapatorias y un paddock preparado para eventos de gran afluencia."
                ),
                new TrackSeed(
                        NAVARRA_TRACK_NAME,
                        "navarra",
                        "Los Arcos, Navarra, España",
                        "Circuito rápido y variado del norte de España, popular entre clubes y organizadores que buscan fines de semana completos."
                ),
                new TrackSeed(
                        ALBACETE_TRACK_NAME,
                        "albacete",
                        "Albacete, Castilla-La Mancha, España",
                        "Trazado muy apreciado por su equilibrio entre técnica y velocidad, habitual para entrenamientos y tandas de aficionados."
                ),
                new TrackSeed(
                        MONTEBLANCO_TRACK_NAME,
                        "monteblanco",
                        "La Palma del Condado, Huelva, España",
                        "Instalación moderna del sur peninsular, utilizada para track days, desarrollo de vehículos y jornadas corporativas."
                ),
                new TrackSeed(
                        CARTAGENA_TRACK_NAME,
                        "cartagena",
                        "Fuente Álamo, Murcia, España",
                        "Circuito compacto y técnico del sureste español, ideal para tandas privadas y sesiones con coches ligeros."
                ),
                new TrackSeed(
                        ESTORIL_TRACK_NAME,
                        "estoril",
                        "Cascais, Lisboa, Portugal",
                        "Circuito histórico portugués junto al Atlántico, muy atractivo para eventos ibéricos y tandas con ambiente premium."
                ),
                new TrackSeed(
                        BRAGA_TRACK_NAME,
                        "braga",
                        "Braga, Norte, Portugal",
                        "Trazado portugués de longitud contenida, perfecto para jornadas de comunidad, formación y sesiones técnicas."
                ),
                new TrackSeed(
                        VILA_REAL_TRACK_NAME,
                        "vila_real",
                        "Vila Real, Norte, Portugal",
                        "Recorrido urbano portugués de carácter rápido y exigente, plausible para eventos demo y experiencias especiales."
                ),
                new TrackSeed(
                        BOAVISTA_TRACK_NAME,
                        "boavista",
                        "Porto, Norte, Portugal",
                        "Circuito urbano icónico del entorno de Porto, útil como referencia para eventos especiales de exhibición y comunidad."
                ),
                new TrackSeed(
                        SPA_TRACK_NAME,
                        "spa",
                        "Stavelot, Lieja, Bélgica",
                        "Uno de los circuitos más emblemáticos de Europa, muy asociado a tandas premium y a experiencias de alto nivel."
                ),
                new TrackSeed(
                        MUGELLO_TRACK_NAME,
                        "mugello",
                        "Scarperia e San Piero, Toscana, Italia",
                        "Trazado italiano muy apreciado por su desnivel, enlazadas rápidas y ambiente de paddock de primer nivel."
                ),
                new TrackSeed(
                        PAUL_RICARD_TRACK_NAME,
                        "paul_ricard",
                        "Le Castellet, Provenza-Alpes-Costa Azul, Francia",
                        "Circuito francés moderno y versátil, frecuentemente utilizado para entrenamientos privados y eventos internacionales."
                )
        );
    }

    private List<ServiceSeed> serviceSeeds() {
        return List.of(
                new ServiceSeed(
                        BOX_RENTAL_SERVICE_NAME,
                        "Reserva de box privado para el evento.",
                        true,
                        false
                ),
                new ServiceSeed(
                        COVERED_PADDOCK_SERVICE_NAME,
                        "Uso de plaza en paddock cubierto durante la jornada.",
                        true,
                        false
                ),
                new ServiceSeed(
                        NOISE_CONTROL_SERVICE_NAME,
                        "Supervisión y medición del nivel sonoro del vehículo en pista.",
                        true,
                        false
                ),
                new ServiceSeed(
                        SKIDPAD_SERVICE_NAME,
                        "Acceso a ejercicios en superficie deslizante dentro del circuito.",
                        true,
                        false
                ),
                new ServiceSeed(
                        EVENT_PHOTOGRAPHY_SERVICE_NAME,
                        "Cobertura fotográfica profesional de la jornada.",
                        false,
                        true
                ),
                new ServiceSeed(
                        EVENT_VIDEO_SERVICE_NAME,
                        "Edición de vídeo con los mejores momentos del evento.",
                        false,
                        true
                ),
                new ServiceSeed(
                        CATERING_SERVICE_NAME,
                        "Servicio de comida y bebida para asistentes y participantes.",
                        false,
                        true
                ),
                new ServiceSeed(
                        WELCOME_PACK_SERVICE_NAME,
                        "Pack de bienvenida con acreditación y material del evento.",
                        false,
                        true
                ),
                new ServiceSeed(
                        INSTRUCTOR_SERVICE_NAME,
                        "Sesión de asesoramiento y acompañamiento con instructor.",
                        false,
                        true
                ),
                new ServiceSeed(
                        SECOND_DRIVER_INSURANCE_SERVICE_NAME,
                        "Cobertura adicional para incluir un segundo conductor autorizado.",
                        false,
                        true
                ),
                new ServiceSeed(
                        COPILOT_INSURANCE_SERVICE_NAME,
                        "Cobertura adicional para incluir copiloto durante la actividad.",
                        false,
                        true
                ),
                new ServiceSeed(
                        TRANSPONDER_TIMING_SERVICE_NAME,
                        "Sistema de cronometraje con transponder para registrar tiempos por vuelta.",
                        true,
                        true
                )
        );
    }

    private String eventDescription(String organizerLegalName, String trackName, String focus) {
        return "Jornada organizada por "
                + organizerLegalName
                + " en "
                + trackName
                + ". "
                + focus
                + " La experiencia incluye briefing de seguridad, control de accesos, organización de tandas durante todo el día y asistencia en paddock para participantes y acompañantes.";
    }

    private List<EventSeed> pastEventSeeds() {
        return List.of(
                new EventSeed(
                        TRACKEVENTS_LEGAL_NAME,
                        JARAMA_TRACK_NAME,
                        PAST_JARAMA_EVENT_DATE,
                        price("180.00"),
                        55,
                        eventDescription(
                                TRACKEVENTS_LEGAL_NAME,
                                JARAMA_TRACK_NAME,
                                "Track day enfocado a turismos deportivos con tandas estables, ambiente de club y ritmo progresivo para rodar con seguridad en un trazado técnico."
                        )
                ),
                new EventSeed(
                        RACINGPRO_LEGAL_NAME,
                        CALAFAT_TRACK_NAME,
                        PAST_CALAFAT_EVENT_DATE,
                        price("145.00"),
                        35,
                        eventDescription(
                                RACINGPRO_LEGAL_NAME,
                                CALAFAT_TRACK_NAME,
                                "Programa orientado a coches ligeros y compactos deportivos, con sesiones fluidas y especial atención a la convivencia en pista junto al mar."
                        )
                ),
                new EventSeed(
                        IBERIAN_MOTORSPORT_LEGAL_NAME,
                        RICARDO_TORMO_TRACK_NAME,
                        PAST_RICARDO_TORMO_EVENT_DATE,
                        price("210.00"),
                        70,
                        eventDescription(
                                IBERIAN_MOTORSPORT_LEGAL_NAME,
                                RICARDO_TORMO_TRACK_NAME,
                                "Evento de aforo amplio pensado para rodar por niveles, aprovechar la amplitud del trazado y combinar ritmo alto con una operativa muy ordenada."
                        )
                ),
                new EventSeed(
                        TRACKEVENTS_LEGAL_NAME,
                        ALGARVE_TRACK_NAME,
                        PAST_ALGARVE_EVENT_DATE,
                        price("260.00"),
                        45,
                        eventDescription(
                                TRACKEVENTS_LEGAL_NAME,
                                ALGARVE_TRACK_NAME,
                                "Jornada premium para coches de calle muy prestacionales y deportivos preparados, con grupos reducidos y mucho tiempo real de pista en un circuito internacional."
                        )
                ),
                new EventSeed(
                        APEX_IBERIA_LEGAL_NAME,
                        BARCELONA_TRACK_NAME,
                        PAST_BARCELONA_EVENT_DATE,
                        price("245.00"),
                        68,
                        eventDescription(
                                APEX_IBERIA_LEGAL_NAME,
                                BARCELONA_TRACK_NAME,
                                "Encuentro de corte premium con tandas largas, boxes activos y un formato ideal para quien busca una experiencia de gran premio sin perder el enfoque amateur."
                        )
                ),
                new EventSeed(
                        TRACKLIMITS_IBERIA_LEGAL_NAME,
                        JEREZ_TRACK_NAME,
                        PAST_JEREZ_EVENT_DATE,
                        price("195.00"),
                        50,
                        eventDescription(
                                TRACKLIMITS_IBERIA_LEGAL_NAME,
                                JEREZ_TRACK_NAME,
                                "Track day equilibrado para aficionados habituales y debutantes con referencias claras, ambiente andaluz de paddock y una configuración muy agradecida para aprender."
                        )
                ),
                new EventSeed(
                        LUSITANIA_RACING_LEGAL_NAME,
                        ESTORIL_TRACK_NAME,
                        PAST_ESTORIL_EVENT_DATE,
                        price("215.00"),
                        48,
                        eventDescription(
                                LUSITANIA_RACING_LEGAL_NAME,
                                ESTORIL_TRACK_NAME,
                                "Cita ibérica pensada para GT y deportivos modernos, con especial énfasis en la regularidad, la trazada limpia y el aprovechamiento de una pista muy técnica."
                        )
                ),
                new EventSeed(
                        MEDITERRANEAN_MOTORSPORT_LEGAL_NAME,
                        MOTORLAND_TRACK_NAME,
                        PAST_MOTORLAND_EVENT_DATE,
                        PRICE_225_00,
                        60,
                        eventDescription(
                                MEDITERRANEAN_MOTORSPORT_LEGAL_NAME,
                                MOTORLAND_TRACK_NAME,
                                "Evento de ritmo medio-alto concebido para sesiones consistentes, escapatorias amplias y una jornada completa en uno de los circuitos más versátiles del calendario."
                        )
                )
        );
    }

    private List<EventSeed> futureEventSeeds() {
        return List.of(
                new EventSeed(
                        TRACKEVENTS_LEGAL_NAME,
                        JARAMA_TRACK_NAME,
                        FUTURE_JARAMA_EVENT_DATE,
                        price("205.00"),
                        60,
                        eventDescription(
                                TRACKEVENTS_LEGAL_NAME,
                                JARAMA_TRACK_NAME,
                                "Próxima cita del calendario pensada para turismos y deportivos de calle con grupos por ritmo, buen tiempo real de pista y una operativa ágil desde primera hora."
                        )
                ),
                new EventSeed(
                        RACINGPRO_LEGAL_NAME,
                        CALAFAT_TRACK_NAME,
                        FUTURE_CALAFAT_EVENT_DATE,
                        price("160.00"),
                        38,
                        eventDescription(
                                RACINGPRO_LEGAL_NAME,
                                CALAFAT_TRACK_NAME,
                                "Formato desenfadado para coches ligeros y tandas fluidas, ideal para quien busca mar, técnica y una jornada muy aprovechable sin excesiva saturación."
                        )
                ),
                new EventSeed(
                        LUSITANIA_RACING_LEGAL_NAME,
                        ESTORIL_TRACK_NAME,
                        FUTURE_ESTORIL_EVENT_DATE,
                        price("220.00"),
                        48,
                        eventDescription(
                                LUSITANIA_RACING_LEGAL_NAME,
                                ESTORIL_TRACK_NAME,
                                "Edición orientada a deportivos modernos y GT de uso ocasional en circuito, con un ritmo creciente durante el día y mucho trabajo de referencias."
                        )
                ),
                new EventSeed(
                        RACINGPRO_LEGAL_NAME,
                        GUADIX_TRACK_NAME,
                        FUTURE_GUADIX_EVENT_DATE,
                        price("150.00"),
                        6,
                        eventDescription(
                                RACINGPRO_LEGAL_NAME,
                                GUADIX_TRACK_NAME,
                                "Evento pequeño y muy enfocado a pilotos que quieren rodar con espacio, aprender el trazado con calma y aprovechar un aforo reducido hasta el último minuto."
                        )
                ),
                new EventSeed(
                        TRACKLIMITS_IBERIA_LEGAL_NAME,
                        JEREZ_TRACK_NAME,
                        FUTURE_JEREZ_EVENT_DATE,
                        price("198.00"),
                        52,
                        eventDescription(
                                TRACKLIMITS_IBERIA_LEGAL_NAME,
                                JEREZ_TRACK_NAME,
                                "Track day de corte mixto con sitio para rodadores habituales y participantes que pisan circuito por primera vez, aprovechando un trazado rápido y muy legible."
                        )
                ),
                new EventSeed(
                        APEX_IBERIA_LEGAL_NAME,
                        LE_MANS_BUGATTI_TRACK_NAME,
                        FUTURE_BUGATTI_EVENT_DATE,
                        price("275.00"),
                        8,
                        eventDescription(
                                APEX_IBERIA_LEGAL_NAME,
                                LE_MANS_BUGATTI_TRACK_NAME,
                                "Sesión especial para deportivos con aspiración a ritmo alto, aforo muy corto y una operativa pensada para maximizar cada salida en uno de los nombres icónicos del calendario."
                        )
                ),
                new EventSeed(
                        TRACKLIMITS_IBERIA_LEGAL_NAME,
                        MONTEBLANCO_TRACK_NAME,
                        FUTURE_MONTEBLANCO_EVENT_DATE,
                        price("178.00"),
                        14,
                        eventDescription(
                                TRACKLIMITS_IBERIA_LEGAL_NAME,
                                MONTEBLANCO_TRACK_NAME,
                                "Jornada centrada en tandas ordenadas, pista amable para mejorar técnica y boxes activos durante todo el día con un ambiente muy de escuela avanzada."
                        )
                ),
                new EventSeed(
                        APEX_IBERIA_LEGAL_NAME,
                        BARCELONA_TRACK_NAME,
                        FUTURE_BARCELONA_EVENT_DATE,
                        price("255.00"),
                        70,
                        eventDescription(
                                APEX_IBERIA_LEGAL_NAME,
                                BARCELONA_TRACK_NAME,
                                "Evento de gran formato para quien busca una experiencia premium, tiempo real de pista y la sensación de rodar en un trazado de referencia internacional."
                        )
                ),
                new EventSeed(
                        LUSITANIA_RACING_LEGAL_NAME,
                        BRAGA_TRACK_NAME,
                        FUTURE_BRAGA_EVENT_DATE,
                        price("155.00"),
                        12,
                        eventDescription(
                                LUSITANIA_RACING_LEGAL_NAME,
                                BRAGA_TRACK_NAME,
                                "Programa compacto para coches ligeros y deportivos de potencia media, ideal para enlazar muchas vueltas en una pista corta, técnica y muy divertida."
                        )
                ),
                new EventSeed(
                        TRACKEVENTS_LEGAL_NAME,
                        ALGARVE_TRACK_NAME,
                        FUTURE_ALGARVE_EVENT_DATE,
                        price("285.00"),
                        50,
                        eventDescription(
                                TRACKEVENTS_LEGAL_NAME,
                                ALGARVE_TRACK_NAME,
                                "Cita premium del sur de Portugal para coches muy prestacionales, con grupos controlados y margen para trabajar cambios de rasante y frenadas largas."
                        )
                ),
                new EventSeed(
                        LUSITANIA_RACING_LEGAL_NAME,
                        VILA_REAL_TRACK_NAME,
                        FUTURE_VILA_REAL_EVENT_DATE,
                        price("190.00"),
                        6,
                        eventDescription(
                                LUSITANIA_RACING_LEGAL_NAME,
                                VILA_REAL_TRACK_NAME,
                                "Evento singular de aforo muy reducido pensado para aficionados experimentados que buscan una jornada especial en un trazado urbano con carácter propio."
                        )
                ),
                new EventSeed(
                        IBERIAN_MOTORSPORT_LEGAL_NAME,
                        NURBURGRING_TRACK_NAME,
                        FUTURE_NURBURGRING_EVENT_DATE,
                        price("340.00"),
                        12,
                        eventDescription(
                                IBERIAN_MOTORSPORT_LEGAL_NAME,
                                NURBURGRING_TRACK_NAME,
                                "Jornada de resistencia y regularidad para pilotos con experiencia previa, centrada en acumular vueltas limpias y gestionar un circuito tan largo como exigente."
                        )
                ),
                new EventSeed(
                        IBERIAN_MOTORSPORT_LEGAL_NAME,
                        RICARDO_TORMO_TRACK_NAME,
                        FUTURE_RICARDO_TORMO_EVENT_DATE,
                        PRICE_225_00,
                        72,
                        eventDescription(
                                IBERIAN_MOTORSPORT_LEGAL_NAME,
                                RICARDO_TORMO_TRACK_NAME,
                                "Track day amplio y bien escalonado, ideal para compartir pista entre distintos niveles sin perder ritmo y con un paddock muy cómodo para pasar el día."
                        )
                ),
                new EventSeed(
                        TRACKEVENTS_LEGAL_NAME,
                        LE_MANS_SARTHE_TRACK_NAME,
                        FUTURE_SARTHE_EVENT_DATE,
                        price("295.00"),
                        16,
                        eventDescription(
                                TRACKEVENTS_LEGAL_NAME,
                                LE_MANS_SARTHE_TRACK_NAME,
                                "Encuentro muy especial de espíritu endurance, pensado para disfrutar un trazado histórico con respeto por los procedimientos, los relevos y la gestión del ritmo."
                        )
                ),
                new EventSeed(
                        RACINGPRO_LEGAL_NAME,
                        NAVARRA_TRACK_NAME,
                        FUTURE_NAVARRA_EVENT_DATE,
                        price("185.00"),
                        44,
                        eventDescription(
                                RACINGPRO_LEGAL_NAME,
                                NAVARRA_TRACK_NAME,
                                "Formato muy equilibrado para rodar por grupos, trabajar técnica de frenada y sacar partido a un circuito noble y agradecido para coches de calle."
                        )
                ),
                new EventSeed(
                        IBERIAN_MOTORSPORT_LEGAL_NAME,
                        NURBURGRING_GP_TRACK_NAME,
                        FUTURE_NURBURGRING_GP_EVENT_DATE,
                        price("255.00"),
                        7,
                        eventDescription(
                                IBERIAN_MOTORSPORT_LEGAL_NAME,
                                NURBURGRING_GP_TRACK_NAME,
                                "Edición de aforo muy corto para rodadores que quieren pista libre, referencias claras y un día centrado en exprimir cada tanda con margen."
                        )
                ),
                new EventSeed(
                        MEDITERRANEAN_MOTORSPORT_LEGAL_NAME,
                        MOTORLAND_TRACK_NAME,
                        FUTURE_MOTORLAND_EVENT_DATE,
                        price("235.00"),
                        62,
                        eventDescription(
                                MEDITERRANEAN_MOTORSPORT_LEGAL_NAME,
                                MOTORLAND_TRACK_NAME,
                                "Evento de carácter técnico para enlazar sectores muy distintos, ideal para quien quiere pulir trazadas y mantener un ritmo constante durante toda la jornada."
                        )
                ),
                new EventSeed(
                        MEDITERRANEAN_MOTORSPORT_LEGAL_NAME,
                        PAUL_RICARD_TRACK_NAME,
                        FUTURE_PAUL_RICARD_EVENT_DATE,
                        price("265.00"),
                        15,
                        eventDescription(
                                MEDITERRANEAN_MOTORSPORT_LEGAL_NAME,
                                PAUL_RICARD_TRACK_NAME,
                                "Jornada premium con plazas limitadas, ideal para deportivos potentes y pilotos que buscan un circuito seguro, rápido y perfecto para entrenar referencias."
                        )
                ),
                new EventSeed(
                        TRACKLIMITS_IBERIA_LEGAL_NAME,
                        CARTAGENA_TRACK_NAME,
                        FUTURE_CARTAGENA_EVENT_DATE,
                        price("175.00"),
                        40,
                        eventDescription(
                                TRACKLIMITS_IBERIA_LEGAL_NAME,
                                CARTAGENA_TRACK_NAME,
                                "Track day mediterráneo con ambiente cercano, mucho tiempo de pista y una configuración ideal para coches compactos, roadsters y tandas muy vivas."
                        )
                ),
                new EventSeed(
                        LUSITANIA_RACING_LEGAL_NAME,
                        BOAVISTA_TRACK_NAME,
                        FUTURE_BOAVISTA_EVENT_DATE,
                        price("168.00"),
                        6,
                        eventDescription(
                                LUSITANIA_RACING_LEGAL_NAME,
                                BOAVISTA_TRACK_NAME,
                                "Sesión urbana de aforo mínimo para una experiencia muy exclusiva, pensada para participantes con experiencia y ganas de rodar con enorme espacio en pista."
                        )
                ),
                new EventSeed(
                        APEX_IBERIA_LEGAL_NAME,
                        ALBACETE_TRACK_NAME,
                        FUTURE_ALBACETE_EVENT_DATE,
                        price("165.00"),
                        36,
                        eventDescription(
                                APEX_IBERIA_LEGAL_NAME,
                                ALBACETE_TRACK_NAME,
                                "Programa muy aprovechable para aficionados que buscan continuidad, buena visibilidad y un trazado perfecto para repetir vueltas y mejorar confianza."
                        )
                ),
                new EventSeed(
                        IBERIAN_MOTORSPORT_LEGAL_NAME,
                        SPA_TRACK_NAME,
                        FUTURE_SPA_EVENT_DATE,
                        price("325.00"),
                        58,
                        eventDescription(
                                IBERIAN_MOTORSPORT_LEGAL_NAME,
                                SPA_TRACK_NAME,
                                "Cita de gran formato para coches muy prestacionales y pilotos acostumbrados a circuitos rápidos, con el atractivo de una de las pistas más icónicas del mundo."
                        )
                ),
                new EventSeed(
                        LUSITANIA_RACING_LEGAL_NAME,
                        MUGELLO_TRACK_NAME,
                        FUTURE_MUGELLO_EVENT_DATE,
                        price("310.00"),
                        56,
                        eventDescription(
                                LUSITANIA_RACING_LEGAL_NAME,
                                MUGELLO_TRACK_NAME,
                                "Evento de estilo premium y vocación internacional, pensado para disfrutar enlazadas rápidas, desnivel y una experiencia muy completa de track day europeo."
                        )
                )
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
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 107215L, ALPINE_A110_R_VEHICLE),
                new LapTimeSeed(JUANJE_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 111842L, BMW_M2_VEHICLE),
                new LapTimeSeed(LATEBRAKER_88_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 114960L, TOYOTA_GR86_VEHICLE),
                new LapTimeSeed(FLATOUT_MARTA_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 113920L, "Audi RS3"),
                new LapTimeSeed(KERB_RIDER_DISPLAY_NAME, JARAMA_TRACK_NAME, LocalDate.of(2026, 3, 8), 116540L, HYUNDAI_I30_N_VEHICLE),

                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 102480L, PORSCHE_911_GT3_VEHICLE),
                new LapTimeSeed(CARLOS_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 111965L, MINI_JOHN_COOPER_WORKS_VEHICLE),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 115410L, "Renault Megane RS"),
                new LapTimeSeed(HEELTOE_DANI_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 113240L, TOYOTA_GR_YARIS_VEHICLE),
                new LapTimeSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, RICARDO_TORMO_TRACK_NAME, LocalDate.of(2026, 3, 12), 117030L, ALPINE_A110_VEHICLE),

                new LapTimeSeed(MARIA_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 95620L, HYUNDAI_I30_N_VEHICLE),
                new LapTimeSeed(JUANJE_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 98640L, "Mazda MX-5 NA 1.8"),
                new LapTimeSeed(REDFLAG_INES_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 100280L, "Volkswagen Golf GTI Clubsport"),
                new LapTimeSeed(CHICANE_CHASER_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 101940L, "Suzuki Swift Sport"),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, CALAFAT_TRACK_NAME, LocalDate.of(2026, 3, 15), 97890L, "Lotus Exige S"),

                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 101870L, ALPINE_A110_R_VEHICLE),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 104450L, "CUPRA Leon VZ"),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 109260L, HONDA_CIVIC_TYPE_R_VEHICLE),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 106980L, TOYOTA_GR_YARIS_VEHICLE),
                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, GUADIX_TRACK_NAME, LocalDate.of(2026, 3, 18), 108440L, "BMW M135i"),

                new LapTimeSeed(JUANJE_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 121930L, "Porsche Cayman S"),
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 117640L, "Aston Martin Vantage"),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 124850L, "BMW M240i"),
                new LapTimeSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 123420L, "Porsche 911 Carrera T"),
                new LapTimeSeed(PADDOCK_PAULA_DISPLAY_NAME, ALGARVE_TRACK_NAME, LocalDate.of(2026, 3, 20), 126780L, ALPINE_A110_S_VEHICLE),

                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, LE_MANS_SARTHE_TRACK_NAME, LocalDate.of(2026, 4, 11), 221340L, "Aston Martin Vantage GT8"),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, LE_MANS_SARTHE_TRACK_NAME, LocalDate.of(2026, 4, 11), 228760L, "Porsche 911 GT3 RS"),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, LE_MANS_SARTHE_TRACK_NAME, LocalDate.of(2026, 4, 11), 236420L, "BMW M4 CSL"),
                new LapTimeSeed(CURB_ATTACK_DISPLAY_NAME, LE_MANS_SARTHE_TRACK_NAME, LocalDate.of(2026, 4, 11), 244110L, "Chevrolet Corvette C8"),
                new LapTimeSeed(HEELTOE_DANI_DISPLAY_NAME, LE_MANS_SARTHE_TRACK_NAME, LocalDate.of(2026, 4, 11), 248950L, "Toyota Supra GR"),

                new LapTimeSeed(PADDOCK_PAULA_DISPLAY_NAME, LE_MANS_BUGATTI_TRACK_NAME, LocalDate.of(2026, 4, 12), 111860L, ALPINE_A110_S_VEHICLE),
                new LapTimeSeed(LATEBRAKER_88_DISPLAY_NAME, LE_MANS_BUGATTI_TRACK_NAME, LocalDate.of(2026, 4, 12), 114430L, TOYOTA_GR86_VEHICLE),
                new LapTimeSeed(GRIDWALKER_DISPLAY_NAME, LE_MANS_BUGATTI_TRACK_NAME, LocalDate.of(2026, 4, 12), 116980L, HYUNDAI_I30_N_VEHICLE),
                new LapTimeSeed(PITLANE_JUNKIE_DISPLAY_NAME, LE_MANS_BUGATTI_TRACK_NAME, LocalDate.of(2026, 4, 12), 118220L, BMW_M2_VEHICLE),
                new LapTimeSeed(FLATOUT_MARTA_DISPLAY_NAME, LE_MANS_BUGATTI_TRACK_NAME, LocalDate.of(2026, 4, 12), 119640L, MAZDA_MX5_ND_VEHICLE),

                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, NURBURGRING_GP_TRACK_NAME, LocalDate.of(2026, 4, 13), 116240L, PORSCHE_911_GT3_VEHICLE),
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, NURBURGRING_GP_TRACK_NAME, LocalDate.of(2026, 4, 13), 114920L, "Aston Martin Vantage"),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, NURBURGRING_GP_TRACK_NAME, LocalDate.of(2026, 4, 13), 121830L, "BMW M4 Competition"),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, NURBURGRING_GP_TRACK_NAME, LocalDate.of(2026, 4, 13), 123970L, HONDA_CIVIC_TYPE_R_VEHICLE),
                new LapTimeSeed(CURVA_PERALTADA_DISPLAY_NAME, NURBURGRING_GP_TRACK_NAME, LocalDate.of(2026, 4, 13), 126540L, TOYOTA_GR_YARIS_VEHICLE),

                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, BARCELONA_TRACK_NAME, LocalDate.of(2026, 3, 22), 109930L, "Porsche 718 Cayman GT4"),
                new LapTimeSeed(CURVA_PERALTADA_DISPLAY_NAME, BARCELONA_TRACK_NAME, LocalDate.of(2026, 3, 22), 118770L, "BMW M3 E46"),
                new LapTimeSeed(GRIDWALKER_DISPLAY_NAME, BARCELONA_TRACK_NAME, LocalDate.of(2026, 3, 22), 121580L, "SEAT Leon Cupra"),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, BARCELONA_TRACK_NAME, LocalDate.of(2026, 3, 22), 116240L, "Porsche 911 Carrera S"),
                new LapTimeSeed(LATEBRAKER_88_DISPLAY_NAME, BARCELONA_TRACK_NAME, LocalDate.of(2026, 3, 22), 119640L, "Renault Megane RS Trophy"),

                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, JEREZ_TRACK_NAME, LocalDate.of(2026, 3, 24), 119460L, "Audi TTS"),
                new LapTimeSeed(PITLANE_JUNKIE_DISPLAY_NAME, JEREZ_TRACK_NAME, LocalDate.of(2026, 3, 24), 123820L, TOYOTA_GR_YARIS_VEHICLE),
                new LapTimeSeed(BRAKEPOINT_NORA_DISPLAY_NAME, JEREZ_TRACK_NAME, LocalDate.of(2026, 3, 24), 126910L, MAZDA_MX5_ND_VEHICLE),
                new LapTimeSeed(HEELTOE_DANI_DISPLAY_NAME, JEREZ_TRACK_NAME, LocalDate.of(2026, 3, 24), 125220L, HYUNDAI_I30_N_VEHICLE),
                new LapTimeSeed(TRACKRAT_77_DISPLAY_NAME, JEREZ_TRACK_NAME, LocalDate.of(2026, 3, 24), 127480L, "BMW 128ti"),

                new LapTimeSeed(CURB_ATTACK_DISPLAY_NAME, MOTORLAND_TRACK_NAME, LocalDate.of(2026, 3, 26), 132210L, "BMW M2 Competition"),
                new LapTimeSeed(STINTMASTER_DISPLAY_NAME, MOTORLAND_TRACK_NAME, LocalDate.of(2026, 3, 26), 129740L, ALPINE_A110_S_VEHICLE),
                new LapTimeSeed(MARIA_DISPLAY_NAME, MOTORLAND_TRACK_NAME, LocalDate.of(2026, 3, 26), 137380L, "Subaru BRZ"),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, MOTORLAND_TRACK_NAME, LocalDate.of(2026, 3, 26), 134860L, HONDA_CIVIC_TYPE_R_VEHICLE),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, MOTORLAND_TRACK_NAME, LocalDate.of(2026, 3, 26), 131980L, ALPINE_A110_R_VEHICLE),

                new LapTimeSeed(CHICANE_CHASER_DISPLAY_NAME, NAVARRA_TRACK_NAME, LocalDate.of(2026, 3, 28), 111320L, "Renault Clio RS"),
                new LapTimeSeed(KERB_RIDER_DISPLAY_NAME, NAVARRA_TRACK_NAME, LocalDate.of(2026, 3, 28), 114470L, "Ford Fiesta ST"),
                new LapTimeSeed(CURVA_PERALTADA_DISPLAY_NAME, NAVARRA_TRACK_NAME, LocalDate.of(2026, 3, 28), 116880L, "Toyota GT86"),
                new LapTimeSeed(GRIDWALKER_DISPLAY_NAME, NAVARRA_TRACK_NAME, LocalDate.of(2026, 3, 28), 115630L, MINI_JOHN_COOPER_WORKS_VEHICLE),
                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, NAVARRA_TRACK_NAME, LocalDate.of(2026, 3, 28), 113980L, "Porsche 718 Cayman"),

                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, ALBACETE_TRACK_NAME, LocalDate.of(2026, 3, 29), 104530L, "Honda S2000"),
                new LapTimeSeed(LATEBRAKER_88_DISPLAY_NAME, ALBACETE_TRACK_NAME, LocalDate.of(2026, 3, 29), 106920L, "Audi RS3"),
                new LapTimeSeed(PITLANE_JUNKIE_DISPLAY_NAME, ALBACETE_TRACK_NAME, LocalDate.of(2026, 3, 29), 109870L, "Renault Clio Cup"),
                new LapTimeSeed(REDFLAG_INES_DISPLAY_NAME, ALBACETE_TRACK_NAME, LocalDate.of(2026, 3, 29), 108440L, "Mazda MX-5 RF"),
                new LapTimeSeed(CHICANE_CHASER_DISPLAY_NAME, ALBACETE_TRACK_NAME, LocalDate.of(2026, 3, 29), 110920L, TOYOTA_GR86_VEHICLE),

                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, MONTEBLANCO_TRACK_NAME, LocalDate.of(2026, 4, 14), 112860L, "Audi TTS"),
                new LapTimeSeed(BRAKEPOINT_NORA_DISPLAY_NAME, MONTEBLANCO_TRACK_NAME, LocalDate.of(2026, 4, 14), 115420L, MAZDA_MX5_ND_VEHICLE),
                new LapTimeSeed(KERB_RIDER_DISPLAY_NAME, MONTEBLANCO_TRACK_NAME, LocalDate.of(2026, 4, 14), 117350L, "Hyundai i20 N"),
                new LapTimeSeed(HEELTOE_DANI_DISPLAY_NAME, MONTEBLANCO_TRACK_NAME, LocalDate.of(2026, 4, 14), 116180L, ALPINE_A110_VEHICLE),
                new LapTimeSeed(TRACKRAT_77_DISPLAY_NAME, MONTEBLANCO_TRACK_NAME, LocalDate.of(2026, 4, 14), 118990L, "BMW 330i"),

                new LapTimeSeed(TRACKRAT_77_DISPLAY_NAME, CARTAGENA_TRACK_NAME, LocalDate.of(2026, 3, 30), 103240L, "Lotus Elise S"),
                new LapTimeSeed(FLATOUT_MARTA_DISPLAY_NAME, CARTAGENA_TRACK_NAME, LocalDate.of(2026, 3, 30), 105910L, "Mini Cooper S"),
                new LapTimeSeed(TYRESMOKE_LUCIA_DISPLAY_NAME, CARTAGENA_TRACK_NAME, LocalDate.of(2026, 3, 30), 108330L, TOYOTA_GR86_VEHICLE),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, CARTAGENA_TRACK_NAME, LocalDate.of(2026, 3, 30), 102650L, PORSCHE_CAYMAN_GTS_VEHICLE),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, CARTAGENA_TRACK_NAME, LocalDate.of(2026, 3, 30), 107420L, "BMW Z4 M40i"),

                new LapTimeSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, ESTORIL_TRACK_NAME, LocalDate.of(2026, 4, 1), 115760L, "Porsche 911 Carrera S"),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, ESTORIL_TRACK_NAME, LocalDate.of(2026, 4, 1), 118140L, "BMW M4"),
                new LapTimeSeed(KERB_RIDER_DISPLAY_NAME, ESTORIL_TRACK_NAME, LocalDate.of(2026, 4, 1), 121020L, "Hyundai Elantra N"),
                new LapTimeSeed(JUANJE_DISPLAY_NAME, ESTORIL_TRACK_NAME, LocalDate.of(2026, 4, 1), 119860L, TOYOTA_GR86_VEHICLE),
                new LapTimeSeed(PADDOCK_PAULA_DISPLAY_NAME, ESTORIL_TRACK_NAME, LocalDate.of(2026, 4, 1), 122740L, "Mini John Cooper Works GP"),

                new LapTimeSeed(REDFLAG_INES_DISPLAY_NAME, BRAGA_TRACK_NAME, LocalDate.of(2026, 4, 2), 94980L, "Abarth 595"),
                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, BRAGA_TRACK_NAME, LocalDate.of(2026, 4, 2), 92840L, "Caterham Seven 420R"),
                new LapTimeSeed(CHICANE_CHASER_DISPLAY_NAME, BRAGA_TRACK_NAME, LocalDate.of(2026, 4, 2), 97350L, "Suzuki Swift Sport"),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, BRAGA_TRACK_NAME, LocalDate.of(2026, 4, 2), 94120L, TOYOTA_GR_YARIS_VEHICLE),
                new LapTimeSeed(TRACKRAT_77_DISPLAY_NAME, BRAGA_TRACK_NAME, LocalDate.of(2026, 4, 2), 96510L, "Renault Clio RS"),

                new LapTimeSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, VILA_REAL_TRACK_NAME, LocalDate.of(2026, 4, 15), 126980L, PORSCHE_CAYMAN_GTS_VEHICLE),
                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, VILA_REAL_TRACK_NAME, LocalDate.of(2026, 4, 15), 128340L, BMW_M2_VEHICLE),
                new LapTimeSeed(PADDOCK_PAULA_DISPLAY_NAME, VILA_REAL_TRACK_NAME, LocalDate.of(2026, 4, 15), 131120L, ALPINE_A110_R_VEHICLE),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, VILA_REAL_TRACK_NAME, LocalDate.of(2026, 4, 15), 130440L, HONDA_CIVIC_TYPE_R_VEHICLE),
                new LapTimeSeed(REDFLAG_INES_DISPLAY_NAME, VILA_REAL_TRACK_NAME, LocalDate.of(2026, 4, 15), 134760L, TOYOTA_GR86_VEHICLE),

                new LapTimeSeed(CURB_ATTACK_DISPLAY_NAME, BOAVISTA_TRACK_NAME, LocalDate.of(2026, 4, 16), 108360L, "Lotus Elise Cup 250"),
                new LapTimeSeed(GRIDWALKER_DISPLAY_NAME, BOAVISTA_TRACK_NAME, LocalDate.of(2026, 4, 16), 109730L, MINI_JOHN_COOPER_WORKS_VEHICLE),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, BOAVISTA_TRACK_NAME, LocalDate.of(2026, 4, 16), 110940L, "BMW M240i"),
                new LapTimeSeed(FLATOUT_MARTA_DISPLAY_NAME, BOAVISTA_TRACK_NAME, LocalDate.of(2026, 4, 16), 111420L, "Mazda MX-5 RF"),
                new LapTimeSeed(LATEBRAKER_88_DISPLAY_NAME, BOAVISTA_TRACK_NAME, LocalDate.of(2026, 4, 16), 112890L, HYUNDAI_I30_N_VEHICLE),

                new LapTimeSeed(CURB_ATTACK_DISPLAY_NAME, SPA_TRACK_NAME, LocalDate.of(2026, 4, 4), 160520L, "BMW M4 CSL"),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, SPA_TRACK_NAME, LocalDate.of(2026, 4, 4), 151880L, PORSCHE_911_GT3_VEHICLE),
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, SPA_TRACK_NAME, LocalDate.of(2026, 4, 4), 149330L, "Aston Martin Vantage GT8"),
                new LapTimeSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, SPA_TRACK_NAME, LocalDate.of(2026, 4, 4), 154920L, "BMW M3 Competition"),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, SPA_TRACK_NAME, LocalDate.of(2026, 4, 4), 157310L, "Porsche Cayman GT4 RS"),

                new LapTimeSeed(APEX_LUSO_DISPLAY_NAME, MUGELLO_TRACK_NAME, LocalDate.of(2026, 4, 6), 128910L, "Ferrari 488 GTB"),
                new LapTimeSeed(OVERSTEER_MIGUEL_DISPLAY_NAME, MUGELLO_TRACK_NAME, LocalDate.of(2026, 4, 6), 133420L, "BMW M3 Touring"),
                new LapTimeSeed(FULLTHROTTLE_EVA_DISPLAY_NAME, MUGELLO_TRACK_NAME, LocalDate.of(2026, 4, 6), 135110L, PORSCHE_CAYMAN_GTS_VEHICLE),
                new LapTimeSeed(REDFLAG_INES_DISPLAY_NAME, MUGELLO_TRACK_NAME, LocalDate.of(2026, 4, 6), 136540L, ALPINE_A110_VEHICLE),
                new LapTimeSeed(PADDOCK_PAULA_DISPLAY_NAME, MUGELLO_TRACK_NAME, LocalDate.of(2026, 4, 6), 138220L, "Porsche 718 Cayman"),

                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, NURBURGRING_TRACK_NAME, LocalDate.of(2026, 4, 8), 456210L, "Porsche 911 GT3 RS"),
                new LapTimeSeed(FERNANDO_ALONSO_DISPLAY_NAME, NURBURGRING_TRACK_NAME, LocalDate.of(2026, 4, 8), 448960L, "Aston Martin Vantage AMR"),
                new LapTimeSeed(STINTMASTER_DISPLAY_NAME, NURBURGRING_TRACK_NAME, LocalDate.of(2026, 4, 8), 487340L, HONDA_CIVIC_TYPE_R_VEHICLE),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, NURBURGRING_TRACK_NAME, LocalDate.of(2026, 4, 8), 472650L, "Porsche Cayman GT4"),
                new LapTimeSeed(CURVA_PERALTADA_DISPLAY_NAME, NURBURGRING_TRACK_NAME, LocalDate.of(2026, 4, 8), 501220L, TOYOTA_GR_YARIS_VEHICLE),

                new LapTimeSeed(PADDOCK_PAULA_DISPLAY_NAME, PAUL_RICARD_TRACK_NAME, LocalDate.of(2026, 4, 10), 134570L, ALPINE_A110_S_VEHICLE),
                new LapTimeSeed(CARLOS_DISPLAY_NAME, PAUL_RICARD_TRACK_NAME, LocalDate.of(2026, 4, 10), 131240L, BMW_M2_VEHICLE),
                new LapTimeSeed(APEXHUNTER_DISPLAY_NAME, PAUL_RICARD_TRACK_NAME, LocalDate.of(2026, 4, 10), 129880L, "Porsche 718 Cayman GT4"),
                new LapTimeSeed(ALEX_PALAU_DISPLAY_NAME, PAUL_RICARD_TRACK_NAME, LocalDate.of(2026, 4, 10), 128940L, "Porsche 911 GT3 Touring"),
                new LapTimeSeed(BOXBOX_RAUL_DISPLAY_NAME, PAUL_RICARD_TRACK_NAME, LocalDate.of(2026, 4, 10), 133510L, "Alpine A110 GT")
        );
    }

    private void seedMessages() {
        createMessageIfMissing(
                JUANJE_DISPLAY_NAME,
                TRACKEVENTS_DISPLAY_NAME,
                LocalDateTime.of(2026, 3, 24, 18, 30),
                false,
                UNREAD_MESSAGE_SUBJECT,
                UNREAD_MESSAGE_CONTENT
        );
    }

    private void createOrganizerServiceIfMissing(String legalName, String serviceName) {
        Organizer organizer = findOrganizerByLegalNameOrThrow(legalName);
        Service service = findServiceByNameOrThrow(serviceName);

        if (organizerServiceRepository.findByOrganizerIdUserAndServiceId(organizer.getIdUser(), service.getId()).isPresent()) {
            organizerServiceRepository.findByOrganizerIdUserAndServiceId(organizer.getIdUser(), service.getId())
                    .ifPresent(existingOrganizerService -> {
                        if (!Boolean.TRUE.equals(existingOrganizerService.getEnabled())) {
                            existingOrganizerService.setEnabled(true);
                            organizerServiceRepository.save(existingOrganizerService);
                        }
                    });
            return;
        }

        OrganizerService organizerService = new OrganizerService();
        organizerService.setOrganizer(organizer);
        organizerService.setService(service);
        organizerService.setEnabled(true);
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
                                      Integer maxParticipants,
                                      String description) {
        Organizer organizer = findOrganizerByLegalNameOrThrow(organizerLegalName);
        Track track = findTrackByNameOrThrow(trackName);

        Event existingEvent = eventRepository.findByTrackIdAndEventDate(track.getId(), eventDate).orElse(null);

        if (existingEvent != null) {
            if (!Objects.equals(existingEvent.getDescription(), description)) {
                existingEvent.setDescription(description);
                eventRepository.save(existingEvent);
            }
            return;
        }

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(eventDate);
        event.setBasePrice(basePrice);
        event.setMaxParticipants(maxParticipants);
        event.setDescription(description);
        eventRepository.save(event);
    }

    private void createEventBookingIfMissing(String attendeeDisplayName,
                                             String trackName,
                                             LocalDate eventDate,
                                             LocalDateTime bookedAt) {
        User attendee = findEventAttendeeByDisplayNameOrThrow(attendeeDisplayName);
        Event event = findEventByTrackAndDateOrThrow(trackName, eventDate);
        boolean isVisible = shouldExposeBookingInPublicProfile(attendeeDisplayName, eventDate);

        EventBooking existingBooking = eventBookingRepository.findByUserIdAndEventId(attendee.getId(), event.getId())
                .orElse(null);
        if (existingBooking != null) {
            if (existingBooking.isVisible() != isVisible) {
                existingBooking.setVisible(isVisible);
                eventBookingRepository.save(existingBooking);
            }
            return;
        }

        EventBooking eventBooking = new EventBooking();
        eventBooking.setUser(attendee);
        eventBooking.setEvent(event);
        eventBooking.setBookedAt(bookedAt);
        eventBooking.setBasePriceAtPurchase(event.getBasePrice());
        eventBooking.setVisible(isVisible);
        eventBookingRepository.save(eventBooking);
    }

    private boolean shouldExposeBookingInPublicProfile(String attendeeDisplayName, LocalDate eventDate) {
        if (eventDate.isBefore(FUTURE_JARAMA_EVENT_DATE)) {
            return true;
        }

        return !PRIVATE_PROFILE_ATTENDEE_DISPLAY_NAMES.contains(attendeeDisplayName);
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
                                 String cif,
                                 boolean enabled) {
    }

    private record TrackSeed(String name, String shortName, String location, String description) {
    }

    private record ServiceSeed(String name,
                               String description,
                               boolean allowedForTrack,
                               boolean allowedForOrganizer) {
    }

    private record EventSeed(String organizerLegalName,
                             String trackName,
                             LocalDate eventDate,
                             BigDecimal basePrice,
                             int maxParticipants,
                             String description) {
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
