package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventServiceUseCase;
import com.trackfindergarage.backend.application.port.in.EventUseCase;
import com.trackfindergarage.backend.application.port.in.OrganizerEventDraft;
import com.trackfindergarage.backend.application.port.in.OrganizerEventServiceDraft;
import com.trackfindergarage.backend.application.port.in.OrganizerServiceUseCase;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceEventStatsView;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceSnapshot;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceStatsView;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceUseCase;
import com.trackfindergarage.backend.application.port.in.TrackServiceUseCase;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventBookingServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerPersistencePort;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ConflictException;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.EventBookingService;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.Organizer;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementa la lógica del espacio de trabajo del organizador.
 *
 * <p>Reúne catálogo, servicios, eventos y estadísticas en una única vista, y además aplica las
 * reglas de negocio de alta, edición y eliminación de elementos gestionados por el organizador, que en este caso
 * son abundantes.</p>
 */
@org.springframework.stereotype.Service
@Transactional
public class OrganizerWorkspaceService implements OrganizerWorkspaceUseCase {

    private static final String AUTHENTICATED_EMAIL_REQUIRED = "El correo electrónico del usuario autenticado es obligatorio";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "Usuario no encontrado con correo electrónico: ";
    private static final String ORGANIZER_NOT_FOUND_FOR_USER = "Organizador no encontrado para el usuario con id: ";
    private static final String ORGANIZER_ROLE_REQUIRED = "Solo las cuentas de organizador pueden acceder a este espacio de trabajo";
    private static final String ORGANIZER_PENDING_APPROVAL =
            "La cuenta de organizador está pendiente de aprobación";
    private static final String EVENT_DOES_NOT_BELONG_TO_AUTHENTICATED_ORGANIZER =
            "El evento no pertenece al organizador autenticado";
    private static final String EVENT_TRACK_CANNOT_BE_CHANGED =
            "El circuito del evento no puede modificarse una vez creado el evento";
    private static final String EVENT_DATE_CANNOT_BE_CHANGED =
            "La fecha del evento no puede modificarse una vez creado el evento";
    private static final String ONLY_FUTURE_EVENTS_CAN_BE_DELETED =
            "Solo se pueden eliminar eventos futuros";
    private static final String EVENT_WITH_BOOKINGS_CANNOT_BE_DELETED =
            "El evento no puede eliminarse mientras tenga reservas";
    private static final String ORGANIZER_SERVICE_DOES_NOT_BELONG_TO_AUTHENTICATED_ORGANIZER =
            "El servicio de organizador no pertenece al organizador autenticado";
    private static final String ORGANIZER_SERVICE_IN_USE_BY_FUTURE_EVENTS =
            "El servicio de organizador no puede eliminarse mientras esté asociado a eventos futuros";
    private static final String EVENT_SERVICE_ALREADY_HAS_BOOKINGS =
            "El servicio de evento no puede eliminarse porque ya ha sido adquirido";
    private static final String EVENT_SERVICE_SELECTION_MUST_TARGET_EXACTLY_ONE_SOURCE =
            "Cada servicio de evento debe apuntar exactamente a un único origen";
    private static final String DUPLICATED_EVENT_SERVICE_SELECTION =
            "El mismo servicio no puede añadirse dos veces al mismo evento";

    private final UserPersistencePort userPersistencePort;
    private final OrganizerPersistencePort organizerPersistencePort;
    private final EventPersistencePort eventPersistencePort;
    private final EventServicePersistencePort eventServicePersistencePort;
    private final EventBookingPersistencePort eventBookingPersistencePort;
    private final EventBookingServicePersistencePort eventBookingServicePersistencePort;
    private final TrackPersistencePort trackPersistencePort;
    private final ServicePersistencePort servicePersistencePort;
    private final TrackServiceUseCase trackServiceUseCase;
    private final OrganizerServiceUseCase organizerServiceUseCase;
    private final EventUseCase eventUseCase;
    private final EventServiceUseCase eventServiceUseCase;

    public OrganizerWorkspaceService(UserPersistencePort userPersistencePort,
                                     OrganizerPersistencePort organizerPersistencePort,
                                     EventPersistencePort eventPersistencePort,
                                     EventServicePersistencePort eventServicePersistencePort,
                                     EventBookingPersistencePort eventBookingPersistencePort,
                                     EventBookingServicePersistencePort eventBookingServicePersistencePort,
                                     TrackPersistencePort trackPersistencePort,
                                     ServicePersistencePort servicePersistencePort,
                                     TrackServiceUseCase trackServiceUseCase,
                                     OrganizerServiceUseCase organizerServiceUseCase,
                                     EventUseCase eventUseCase,
                                     EventServiceUseCase eventServiceUseCase) {
        this.userPersistencePort = userPersistencePort;
        this.organizerPersistencePort = organizerPersistencePort;
        this.eventPersistencePort = eventPersistencePort;
        this.eventServicePersistencePort = eventServicePersistencePort;
        this.eventBookingPersistencePort = eventBookingPersistencePort;
        this.eventBookingServicePersistencePort = eventBookingServicePersistencePort;
        this.trackPersistencePort = trackPersistencePort;
        this.servicePersistencePort = servicePersistencePort;
        this.trackServiceUseCase = trackServiceUseCase;
        this.organizerServiceUseCase = organizerServiceUseCase;
        this.eventUseCase = eventUseCase;
        this.eventServiceUseCase = eventServiceUseCase;
    }

    /**
     * Construye la instantánea completa del workspace para el organizador autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return estado completo del workspace
     */
    @Override
    @Transactional(readOnly = true)
    public OrganizerWorkspaceSnapshot getWorkspace(String authenticatedEmail) {
        Organizer organizer = loadEnabledOrganizer(authenticatedEmail);
        return buildWorkspaceSnapshot(organizer);
    }

    /**
     * Añade un servicio del catálogo al organizador autenticado y devuelve el workspace actualizado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param serviceId identificador del servicio a añadir
     * @return workspace actualizado
     */
    @Override
    public OrganizerWorkspaceSnapshot addOrganizerService(String authenticatedEmail, Long serviceId) {
        Organizer organizer = loadEnabledOrganizer(authenticatedEmail);

        com.trackfindergarage.backend.domain.model.OrganizerService organizerService =
                new com.trackfindergarage.backend.domain.model.OrganizerService();
        organizerService.setOrganizer(organizer);

        Service service = new Service();
        service.setId(serviceId);
        organizerService.setService(service);

        organizerServiceUseCase.createOrganizerService(organizerService);
        return buildWorkspaceSnapshot(organizer);
    }

    /**
     * Elimina un servicio propio del organizador si no está comprometido en eventos futuros.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param organizerServiceId identificador de la asignación a eliminar
     * @return workspace actualizado
     */
    @Override
    public OrganizerWorkspaceSnapshot removeOrganizerService(String authenticatedEmail, Long organizerServiceId) {
        Organizer organizer = loadEnabledOrganizer(authenticatedEmail);
        com.trackfindergarage.backend.domain.model.OrganizerService organizerService =
                organizerServiceUseCase.getOrganizerServiceById(organizerServiceId);

        if (!Objects.equals(organizerService.getOrganizer().getIdUser(), organizer.getIdUser())) {
            throw new AccessDeniedException(ORGANIZER_SERVICE_DOES_NOT_BELONG_TO_AUTHENTICATED_ORGANIZER);
        }

        List<EventService> attachedEventServices = eventServicePersistencePort.findByOrganizerServiceId(organizerServiceId);
        boolean attachedToFutureEvents = attachedEventServices.stream()
                .map(EventService::getEvent)
                .filter(Objects::nonNull)
                .anyMatch(event -> event.getEventDate() != null && event.getEventDate().isAfter(LocalDate.now()));

        if (attachedToFutureEvents) {
            throw new ConflictException(ORGANIZER_SERVICE_IN_USE_BY_FUTURE_EVENTS);
        }

        organizerServiceUseCase.deleteOrganizerService(organizerServiceId);
        return buildWorkspaceSnapshot(organizer);
    }

    /**
     * Crea un evento nuevo para el organizador y sincroniza sus servicios adicionales.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param draft borrador con los datos del evento
     * @return workspace actualizado
     */
    @Override
    public OrganizerWorkspaceSnapshot createEvent(String authenticatedEmail, OrganizerEventDraft draft) {
        Organizer organizer = loadEnabledOrganizer(authenticatedEmail);
        validateEventServiceDrafts(draft.services());

        Event createdEvent = eventUseCase.createEvent(buildEventDomain(organizer.getIdUser(), draft));
        syncEventServices(createdEvent.getId(), draft.services());

        return buildWorkspaceSnapshot(organizer);
    }

    /**
     * Actualiza un evento existente del organizador y sincroniza sus servicios.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param eventId identificador del evento a modificar
     * @param draft borrador con los nuevos datos del evento
     * @return workspace actualizado
     */
    @Override
    public OrganizerWorkspaceSnapshot updateEvent(String authenticatedEmail, Long eventId, OrganizerEventDraft draft) {
        Organizer organizer = loadEnabledOrganizer(authenticatedEmail);
        validateEventServiceDrafts(draft.services());

        Event existingEvent = eventUseCase.getEventById(eventId);
        ensureEventBelongsToOrganizer(existingEvent, organizer);
        validateEditableEventFields(existingEvent, draft);

        eventUseCase.updateEvent(eventId, buildEventDomain(organizer.getIdUser(), draft));
        syncEventServices(eventId, draft.services());

        return buildWorkspaceSnapshot(organizer);
    }

    /**
     * Elimina un evento futuro del organizador autenticado solo si todavía no tiene reservas.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param eventId identificador del evento a eliminar
     * @return workspace actualizado
     */
    @Override
    public OrganizerWorkspaceSnapshot deleteEvent(String authenticatedEmail, Long eventId) {
        Organizer organizer = loadEnabledOrganizer(authenticatedEmail);
        Event existingEvent = eventUseCase.getEventById(eventId);
        ensureEventBelongsToOrganizer(existingEvent, organizer);

        if (existingEvent.getEventDate() == null || !existingEvent.getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(ONLY_FUTURE_EVENTS_CAN_BE_DELETED);
        }

        if (eventBookingPersistencePort.countByEventId(eventId) > 0) {
            throw new ConflictException(EVENT_WITH_BOOKINGS_CANNOT_BE_DELETED);
        }

        eventServiceUseCase.getEventServicesByEventId(eventId)
                .forEach(eventService -> eventServiceUseCase.deleteEventService(eventService.getId()));
        eventUseCase.deleteEvent(eventId);

        return buildWorkspaceSnapshot(organizer);
    }

    // Construye la vista completa del workspace a partir de los datos del organizador.
    private OrganizerWorkspaceSnapshot buildWorkspaceSnapshot(Organizer organizer) {
        List<Service> availableServices = loadAvailableServices();
        List<com.trackfindergarage.backend.domain.model.OrganizerService> organizerServices =
                loadOrganizerServices(organizer.getIdUser());
        List<Track> tracks = loadTracks();
        List<TrackService> trackServices = loadTrackServices();
        List<Event> events = loadOrganizerEvents(organizer.getIdUser());
        WorkspaceEventData workspaceEventData = collectWorkspaceEventData(events);

        return new OrganizerWorkspaceSnapshot(
                organizer,
                availableServices,
                organizerServices,
                tracks,
                trackServices,
                events,
                workspaceEventData.eventServicesByEventId(),
                workspaceEventData.bookedEventServiceIdsByEventId(),
                workspaceEventData.stats()
        );
    }

    // Carga el catálogo de servicios que un organizador puede ofrecer.
    private List<Service> loadAvailableServices() {
        return servicePersistencePort.findAllByAllowedForOrganizerTrue()
                .stream()
                .filter(service -> Boolean.TRUE.equals(service.getEnabled()))
                .sorted(Comparator.comparing(Service::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // Carga y ordena los servicios ya asociados al organizador.
    private List<com.trackfindergarage.backend.domain.model.OrganizerService> loadOrganizerServices(Long organizerId) {
        return organizerServiceUseCase.getOrganizerServicesByOrganizerId(organizerId)
                .stream()
                .sorted(Comparator.comparing(
                        assignment -> assignment.getService().getName(),
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
    }

    // Carga y ordena los circuitos disponibles para crear eventos.
    private List<Track> loadTracks() {
        return trackPersistencePort.findAll()
                .stream()
                .sorted(Comparator.comparing(Track::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // Carga y ordena las asociaciones circuito-servicio disponibles.
    private List<TrackService> loadTrackServices() {
        return trackServiceUseCase.getAllTrackServices()
                .stream()
                .sorted(Comparator
                        .comparing((TrackService assignment) -> assignment.getTrack().getName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(assignment -> assignment.getService().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // Carga y ordena los eventos que pertenecen al organizador.
    private List<Event> loadOrganizerEvents(Long organizerId) {
        return eventPersistencePort.findByOrganizerIdUser(organizerId)
                .stream()
                .sorted(Comparator.comparing(Event::getEventDate))
                .toList();
    }

    // Reúne los servicios, las compras y las estadísticas de cada evento del workspace.
    private WorkspaceEventData collectWorkspaceEventData(List<Event> events) {
        Map<Long, List<EventService>> eventServicesByEventId = new HashMap<>();
        Map<Long, Set<Long>> bookedEventServiceIdsByEventId = new HashMap<>();
        WorkspaceStatsAccumulator statsAccumulator = new WorkspaceStatsAccumulator(LocalDate.now());

        for (Event event : events) {
            eventServicesByEventId.put(event.getId(), eventServiceUseCase.getEventServicesByEventId(event.getId()));
            bookedEventServiceIdsByEventId.put(event.getId(), findBookedEventServiceIds(event.getId()));
            statsAccumulator.add(buildEventStats(event));
        }

        return new WorkspaceEventData(
                eventServicesByEventId,
                bookedEventServiceIdsByEventId,
                statsAccumulator.toView()
        );
    }

    // Calcula las métricas agregadas de un evento concreto.
    private OrganizerWorkspaceEventStatsView buildEventStats(Event event) {
        List<EventBooking> bookings = eventBookingPersistencePort.findByEventId(event.getId());
        List<EventBookingService> soldServices = eventBookingServicePersistencePort.findByEventBookingEventId(event.getId());

        BigDecimal baseRevenue = bookings.stream()
                .map(EventBooking::getBasePriceAtPurchase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal serviceRevenue = soldServices.stream()
                .map(EventBookingService::getPriceAtPurchase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalCapacity = event.getMaxParticipants() == null ? 0 : event.getMaxParticipants();
        int remainingCapacity = Math.max(0, totalCapacity - bookings.size());

        return new OrganizerWorkspaceEventStatsView(
                event.getId(),
                event.getTrack() != null ? event.getTrack().getName() : "",
                event.getEventDate(),
                bookings.size(),
                soldServices.size(),
                remainingCapacity,
                totalCapacity,
                baseRevenue,
                serviceRevenue,
                baseRevenue.add(serviceRevenue)
        );
    }

    private record WorkspaceEventData(
            Map<Long, List<EventService>> eventServicesByEventId,
            Map<Long, Set<Long>> bookedEventServiceIdsByEventId,
            OrganizerWorkspaceStatsView stats
    ) {
    }

    private static final class WorkspaceStatsAccumulator {
        private final LocalDate today;
        private final List<OrganizerWorkspaceEventStatsView> eventStats = new ArrayList<>();
        private BigDecimal totalBaseRevenue = BigDecimal.ZERO;
        private BigDecimal totalServiceRevenue = BigDecimal.ZERO;
        private BigDecimal totalGrossRevenue = BigDecimal.ZERO;
        private long totalBookings = 0L;
        private long totalSoldServices = 0L;
        private long futureEvents = 0L;
        private long pastEvents = 0L;
        private int totalCapacity = 0;
        private int totalRemainingCapacity = 0;

        private WorkspaceStatsAccumulator(LocalDate today) {
            this.today = today;
        }

        private void add(OrganizerWorkspaceEventStatsView eventStat) {
            eventStats.add(eventStat);
            totalBaseRevenue = totalBaseRevenue.add(eventStat.baseRevenue());
            totalServiceRevenue = totalServiceRevenue.add(eventStat.serviceRevenue());
            totalGrossRevenue = totalGrossRevenue.add(eventStat.grossRevenue());
            totalBookings += eventStat.bookings();
            totalSoldServices += eventStat.soldServices();
            totalCapacity += eventStat.totalCapacity();
            totalRemainingCapacity += eventStat.remainingCapacity();

            if (eventStat.eventDate() != null && eventStat.eventDate().isAfter(today)) {
                futureEvents++;
            } else {
                pastEvents++;
            }
        }

        private OrganizerWorkspaceStatsView toView() {
            return new OrganizerWorkspaceStatsView(
                    totalBaseRevenue,
                    totalServiceRevenue,
                    totalGrossRevenue,
                    totalBookings,
                    totalSoldServices,
                    futureEvents,
                    pastEvents,
                    totalCapacity,
                    totalRemainingCapacity,
                    eventStats
            );
        }
    }

    // Carga el organizador autenticado y comprueba que su cuenta está aprobada.
    private Organizer loadEnabledOrganizer(String authenticatedEmail) {
        User user = loadAuthenticatedUser(authenticatedEmail);

        if (user.getRole() == null
                || user.getRole().getRoleName() == null
                || !"ORGANIZER".equalsIgnoreCase(user.getRole().getRoleName().trim())) {
            throw new AccessDeniedException(ORGANIZER_ROLE_REQUIRED);
        }

        Organizer organizer = organizerPersistencePort.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZER_NOT_FOUND_FOR_USER + user.getId()));

        if (!Boolean.TRUE.equals(organizer.getEnabled())) {
            throw new AccessDeniedException(ORGANIZER_PENDING_APPROVAL);
        }

        return organizer;
    }

    // Carga y normaliza el usuario autenticado a partir de su correo.
    private User loadAuthenticatedUser(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        return userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail));
    }

    // Construye la entidad Event a partir del borrador recibido desde el workspace.
    private Event buildEventDomain(Long organizerId, OrganizerEventDraft draft) {
        Event event = new Event();

        Organizer organizer = new Organizer();
        organizer.setIdUser(organizerId);

        Track track = new Track();
        track.setId(draft.trackId());

        event.setOrganizer(organizer);
        event.setTrack(track);
        event.setEventDate(draft.eventDate());
        event.setBasePrice(draft.basePrice());
        event.setMaxParticipants(draft.maxParticipants());
        event.setDescription(draft.description());

        return event;
    }

    // Comprueba que el evento realmente pertenece al organizador autenticado.
    private void ensureEventBelongsToOrganizer(Event event, Organizer organizer) {
        if (event.getOrganizer() == null || !Objects.equals(event.getOrganizer().getIdUser(), organizer.getIdUser())) {
            throw new AccessDeniedException(EVENT_DOES_NOT_BELONG_TO_AUTHENTICATED_ORGANIZER);
        }
    }

    // Valida qué campos del evento pueden seguir editándose una vez creado.
    private void validateEditableEventFields(Event existingEvent, OrganizerEventDraft draft) {
        if (existingEvent.getTrack() != null && !Objects.equals(existingEvent.getTrack().getId(), draft.trackId())) {
            throw new ConflictException(EVENT_TRACK_CANNOT_BE_CHANGED);
        }

        if (!Objects.equals(existingEvent.getEventDate(), draft.eventDate())) {
            throw new ConflictException(EVENT_DATE_CANNOT_BE_CHANGED);
        }
    }

    // Valida la selección de servicios del borrador: origen único, precio válido y sin duplicados.
    private void validateEventServiceDrafts(List<OrganizerEventServiceDraft> drafts) {
        Set<String> seenKeys = new HashSet<>();

        for (OrganizerEventServiceDraft draft : normalizeDraftServices(drafts)) {
            boolean hasTrackService = draft.trackServiceId() != null;
            boolean hasOrganizerService = draft.organizerServiceId() != null;

            if (hasTrackService == hasOrganizerService) {
                throw new IllegalArgumentException(EVENT_SERVICE_SELECTION_MUST_TARGET_EXACTLY_ONE_SOURCE);
            }

            if (draft.price() == null || draft.price().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Each selected service must define a valid price");
            }

            String key = hasTrackService
                    ? "track:" + draft.trackServiceId()
                    : "organizer:" + draft.organizerServiceId();

            if (!seenKeys.add(key)) {
                throw new DuplicateResourceException(DUPLICATED_EVENT_SERVICE_SELECTION);
            }
        }
    }

    // Sincroniza los servicios del evento: crea los nuevos, actualiza precios y elimina los sobrantes.
    private void syncEventServices(Long eventId, List<OrganizerEventServiceDraft> requestedServices) {
        List<OrganizerEventServiceDraft> normalizedRequestedServices = normalizeDraftServices(requestedServices);
        List<EventService> existingServices = eventServiceUseCase.getEventServicesByEventId(eventId);
        Map<String, EventService> existingByKey = new HashMap<>();
        Set<String> requestedKeys = new HashSet<>();

        for (EventService existingService : existingServices) {
            existingByKey.put(toServiceKey(existingService), existingService);
        }

        for (OrganizerEventServiceDraft requestedService : normalizedRequestedServices) {
            String serviceKey = toServiceKey(requestedService);
            requestedKeys.add(serviceKey);

            // Crea la asociación si todavía no existía.
            EventService existingService = existingByKey.get(serviceKey);
            if (existingService == null) {
                eventServiceUseCase.createEventService(buildEventServiceDomain(eventId, requestedService));
                continue;
            }

            // Actualiza el servicio sólo si ha cambiado el precio configurado.
            if (existingService.getPrice().compareTo(requestedService.price()) != 0) {
                eventServiceUseCase.updateEventService(existingService.getId(), buildEventServiceDomain(eventId, requestedService));
            }
        }

        for (EventService existingService : existingServices) {
            if (requestedKeys.contains(toServiceKey(existingService))) {
                continue;
            }

            // Impide borrar servicios que ya han sido comprados en reservas.
            if (!eventBookingServicePersistencePort.findByEventServiceId(existingService.getId()).isEmpty()) {
                throw new ConflictException(EVENT_SERVICE_ALREADY_HAS_BOOKINGS);
            }

            eventServiceUseCase.deleteEventService(existingService.getId());
        }
    }

    // Construye la entidad EventService a partir del borrador recibido desde el workspace.
    private EventService buildEventServiceDomain(Long eventId, OrganizerEventServiceDraft draft) {
        EventService eventService = new EventService();

        Event event = new Event();
        event.setId(eventId);
        eventService.setEvent(event);

        if (draft.trackServiceId() != null) {
            TrackService trackService = new TrackService();
            trackService.setId(draft.trackServiceId());
            eventService.setTrackService(trackService);
        }

        if (draft.organizerServiceId() != null) {
            com.trackfindergarage.backend.domain.model.OrganizerService organizerService =
                    new com.trackfindergarage.backend.domain.model.OrganizerService();
            organizerService.setId(draft.organizerServiceId());
            eventService.setOrganizerService(organizerService);
        }

        eventService.setPrice(draft.price());
        return eventService;
    }

    // Normaliza la colección de borradores eliminando nulos y devolviendo lista vacía si no hay datos.
    private List<OrganizerEventServiceDraft> normalizeDraftServices(List<OrganizerEventServiceDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return List.of();
        }

        return drafts.stream().filter(Objects::nonNull).toList();
    }

    // Localiza qué servicios de evento ya han sido adquiridos en reservas.
    private Set<Long> findBookedEventServiceIds(Long eventId) {
        Set<Long> bookedEventServiceIds = new HashSet<>();

        for (EventBookingService soldService : eventBookingServicePersistencePort.findByEventBookingEventId(eventId)) {
            if (soldService.getEventService() != null && soldService.getEventService().getId() != null) {
                bookedEventServiceIds.add(soldService.getEventService().getId());
            }
        }

        return bookedEventServiceIds;
    }

    // Genera la clave lógica de un borrador de servicio para compararlo con el estado persistido.
    private String toServiceKey(OrganizerEventServiceDraft draft) {
        return toServiceKey(draft.trackServiceId(), draft.organizerServiceId());
    }

    // Genera la clave lógica de un servicio de evento ya persistido.
    private String toServiceKey(EventService existingService) {
        return toServiceKey(
                existingService.getTrackService() != null ? existingService.getTrackService().getId() : null,
                existingService.getOrganizerService() != null ? existingService.getOrganizerService().getId() : null
        );
    }

    // Normaliza la identidad de un servicio usando su origen real: circuito u organizador.
    private String toServiceKey(Long trackServiceId, Long organizerServiceId) {
        return trackServiceId != null
                ? "track:" + trackServiceId
                : "organizer:" + organizerServiceId;
    }
}
