package com.trackfindergarage.backend.infrastructure.config;

import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.Role;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.domain.model.User;
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

import java.time.LocalDateTime;

@Component
@Profile("demo")
public class DemoDataSeeder implements CommandLineRunner {

    private static final String TRACKEVENTS_LEGAL_NAME = "TrackEvents S.L.";
    private static final String RACINGPRO_LEGAL_NAME = "RacingPro S.L.";
    private static final String IBERIAN_MOTORSPORT_LEGAL_NAME = "Iberian Motorsport Events S.L.";
    private static final String DEFAULT_STANDARD_USER_PASSWORD = "user123";
    private static final String DEFAULT_ORGANIZER_PASSWORD = "org123";

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

    private final SpringDataRoleRepository roleRepository;
    private final SpringDataUserRepository userRepository;
    private final SpringDataOrganizerRepository organizerRepository;
    private final SpringDataOrganizerServiceRepository organizerServiceRepository;
    private final SpringDataTrackRepository trackRepository;
    private final SpringDataTrackServiceRepository trackServiceRepository;
    private final SpringDataServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(SpringDataRoleRepository roleRepository,
                          SpringDataUserRepository userRepository,
                          SpringDataOrganizerRepository organizerRepository,
                          SpringDataOrganizerServiceRepository organizerServiceRepository,
                          SpringDataTrackRepository trackRepository,
                          SpringDataTrackServiceRepository trackServiceRepository,
                          SpringDataServiceRepository serviceRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
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
        createUser("juanje", "juanje@example.com", "Juanje", "Demo", DEFAULT_STANDARD_USER_PASSWORD, userRole);
        createUser("maria", "maria@example.com", "Maria", "Demo", DEFAULT_STANDARD_USER_PASSWORD, userRole);
        createUser("carlos", "carlos@example.com", "Carlos", "Demo", DEFAULT_STANDARD_USER_PASSWORD, userRole);
        createUser("fernando.alonso", "fernando.alonso@example.com", "Fernando", "Alonso", DEFAULT_STANDARD_USER_PASSWORD, userRole);
        createUser("alex.palau", "alex.palau@example.com", "Alex", "Palou", DEFAULT_STANDARD_USER_PASSWORD, userRole);

        createOrganizer(
                "trackevents",
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
        User user = createUser(displayName, email, name, surname, DEFAULT_ORGANIZER_PASSWORD, organizerRole);

        Organizer organizer = new Organizer();
        organizer.setUser(user);
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

    private Organizer findOrganizerByLegalNameOrThrow(String legalName) {
        return organizerRepository.findByLegalName(legalName)
                .orElseThrow(() -> new IllegalStateException("Organizer not found in demo seed: " + legalName));
    }

    private Track findTrackByNameOrThrow(String trackName) {
        return trackRepository.findByName(trackName)
                .orElseThrow(() -> new IllegalStateException("Track not found in demo seed: " + trackName));
    }

    private Service findServiceByNameOrThrow(String serviceName) {
        return serviceRepository.findByName(serviceName)
                .orElseThrow(() -> new IllegalStateException("Service not found in demo seed: " + serviceName));
    }
}
