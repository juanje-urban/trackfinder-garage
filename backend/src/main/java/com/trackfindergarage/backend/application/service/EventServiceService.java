package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.EventServiceUseCase;
import com.trackfindergarage.backend.application.port.out.EventPersistencePort;
import com.trackfindergarage.backend.application.port.out.EventServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.OrganizerServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.domain.model.OrganizerService;
import com.trackfindergarage.backend.domain.model.TrackService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementa la lógica de asociación de servicios adicionales a un evento.
 *
 * <p>Garantiza que cada asociación apunte a un único origen válido y que ese servicio pertenezca
 * al mismo circuito u organizador del evento.</p>
 */
@org.springframework.stereotype.Service
@Transactional
public class EventServiceService implements EventServiceUseCase {

    private static final String EVENT_SERVICE_NOT_FOUND_WITH_ID = "Servicio de evento no encontrado con id: ";
    private static final String EVENT_NOT_FOUND_WITH_ID = "Evento no encontrado con id: ";
    private static final String TRACK_SERVICE_NOT_FOUND_WITH_ID = "Servicio de circuito no encontrado con id: ";
    private static final String ORGANIZER_SERVICE_NOT_FOUND_WITH_ID = "Servicio de organizador no encontrado con id: ";
    private static final String PRICE_REQUIRED = "El precio es obligatorio";
    private static final String PRICE_MUST_BE_GREATER_THAN_ZERO = "El precio debe ser mayor que 0";
    private static final String EXACTLY_ONE_SERVICE_ASSOCIATION_REQUIRED =
            "Debe proporcionarse exactamente uno de trackServiceId u organizerServiceId";
    private static final String TRACK_SERVICE_ALREADY_EXISTS_FOR_EVENT =
            "El servicio de circuito con id %d ya está asociado al evento con id %d";
    private static final String ORGANIZER_SERVICE_ALREADY_EXISTS_FOR_EVENT =
            "El servicio de organizador con id %d ya está asociado al evento con id %d";
    private static final String TRACK_SERVICE_MUST_BELONG_TO_EVENT_TRACK =
            "El servicio de circuito con id %d no pertenece al mismo circuito que el evento con id %d";
    private static final String ORGANIZER_SERVICE_MUST_BELONG_TO_EVENT_ORGANIZER =
            "El servicio de organizador con id %d no pertenece al mismo organizador que el evento con id %d";

    private final EventServicePersistencePort eventServicePersistencePort;
    private final EventPersistencePort eventPersistencePort;
    private final TrackServicePersistencePort trackServicePersistencePort;
    private final OrganizerServicePersistencePort organizerServicePersistencePort;

    public EventServiceService(EventServicePersistencePort eventServicePersistencePort,
                               EventPersistencePort eventPersistencePort,
                               TrackServicePersistencePort trackServicePersistencePort,
                               OrganizerServicePersistencePort organizerServicePersistencePort) {
        this.eventServicePersistencePort = eventServicePersistencePort;
        this.eventPersistencePort = eventPersistencePort;
        this.trackServicePersistencePort = trackServicePersistencePort;
        this.organizerServicePersistencePort = organizerServicePersistencePort;
    }

    /**
     * Asocia un nuevo servicio a un evento.
     *
     * @param eventService datos de la asociación
     * @return servicio de evento persistido
     */
    @Override
    public EventService createEventService(EventService eventService) {
        validateEventService(eventService);

        Event event = eventPersistencePort.findById(extractEventId(eventService))
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + extractEventId(eventService)));

        TrackService trackService = resolveTrackService(eventService, event, null);
        OrganizerService organizerService = resolveOrganizerService(eventService, event, null);

        eventService.setEvent(event);
        eventService.setTrackService(trackService);
        eventService.setOrganizerService(organizerService);

        return eventServicePersistencePort.save(eventService);
    }

    /**
     * Actualiza una asociación existente entre un evento y un servicio.
     *
     * @param id identificador de la asociación a modificar
     * @param eventService nuevos datos de la asociación
     * @return servicio de evento actualizado
     */
    @Override
    public EventService updateEventService(Long id, EventService eventService) {
        validateEventService(eventService);

        EventService existingEventService = findEventServiceOrThrow(id);

        Event event = eventPersistencePort.findById(extractEventId(eventService))
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + extractEventId(eventService)));

        TrackService trackService = resolveTrackService(eventService, event, id);
        OrganizerService organizerService = resolveOrganizerService(eventService, event, id);

        existingEventService.setEvent(event);
        existingEventService.setTrackService(trackService);
        existingEventService.setOrganizerService(organizerService);
        existingEventService.setPrice(eventService.getPrice());

        return eventServicePersistencePort.save(existingEventService);
    }

    /**
     * Elimina un servicio ya asociado a un evento.
     *
     * @param id identificador de la asociación a eliminar
     */
    @Override
    public void deleteEventService(Long id) {
        EventService eventService = findEventServiceOrThrow(id);
        eventServicePersistencePort.delete(eventService);
    }

    /**
     * Recupera todos los servicios configurados para un evento concreto.
     *
     * @param eventId identificador del evento
     * @return listado de servicios del evento
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventService> getEventServicesByEventId(Long eventId) {
        eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_NOT_FOUND_WITH_ID + eventId));

        return eventServicePersistencePort.findByEventId(eventId);
    }

    //Métodos auxiliares para recuperar y validar datos
    private void validateEventService(EventService eventService) {
        if (eventService.getEvent() == null || eventService.getEvent().getId() == null) {
            throw new IllegalArgumentException("Event id is required");
        }
        if (eventService.getPrice() == null) {
            throw new IllegalArgumentException(PRICE_REQUIRED);
        }
        if (eventService.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(PRICE_MUST_BE_GREATER_THAN_ZERO);
        }

        boolean hasTrackService = eventService.getTrackService() != null && eventService.getTrackService().getId() != null;
        boolean hasOrganizerService =
                eventService.getOrganizerService() != null && eventService.getOrganizerService().getId() != null;

        if (hasTrackService == hasOrganizerService) {
            throw new IllegalArgumentException(EXACTLY_ONE_SERVICE_ASSOCIATION_REQUIRED);
        }
    }

    private TrackService resolveTrackService(EventService eventService, Event event, Long currentEventServiceId) {
        if (eventService.getTrackService() == null || eventService.getTrackService().getId() == null) {
            return null;
        }

        Long trackServiceId = eventService.getTrackService().getId();
        TrackService trackService = trackServicePersistencePort.findById(trackServiceId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_SERVICE_NOT_FOUND_WITH_ID + trackServiceId));

        if (!trackService.getTrack().getId().equals(event.getTrack().getId())) {
            throw new IllegalArgumentException(TRACK_SERVICE_MUST_BELONG_TO_EVENT_TRACK.formatted(trackServiceId, event.getId()));
        }

        eventServicePersistencePort.findByEventIdAndTrackServiceId(event.getId(), trackServiceId)
                .ifPresent(existingEventService -> {
                    if (currentEventServiceId == null || !existingEventService.getId().equals(currentEventServiceId)) {
                        throw new DuplicateResourceException(
                                TRACK_SERVICE_ALREADY_EXISTS_FOR_EVENT.formatted(trackServiceId, event.getId())
                        );
                    }
                });

        return trackService;
    }

    private OrganizerService resolveOrganizerService(EventService eventService, Event event, Long currentEventServiceId) {
        if (eventService.getOrganizerService() == null || eventService.getOrganizerService().getId() == null) {
            return null;
        }

        Long organizerServiceId = eventService.getOrganizerService().getId();
        OrganizerService organizerService = organizerServicePersistencePort.findById(organizerServiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ORGANIZER_SERVICE_NOT_FOUND_WITH_ID + organizerServiceId
                ));

        if (!organizerService.getOrganizer().getIdUser().equals(event.getOrganizer().getIdUser())) {
            throw new IllegalArgumentException(
                    ORGANIZER_SERVICE_MUST_BELONG_TO_EVENT_ORGANIZER.formatted(organizerServiceId, event.getId())
            );
        }

        eventServicePersistencePort.findByEventIdAndOrganizerServiceId(event.getId(), organizerServiceId)
                .ifPresent(existingEventService -> {
                    if (currentEventServiceId == null || !existingEventService.getId().equals(currentEventServiceId)) {
                        throw new DuplicateResourceException(
                                ORGANIZER_SERVICE_ALREADY_EXISTS_FOR_EVENT.formatted(organizerServiceId, event.getId())
                        );
                    }
                });

        return organizerService;
    }

    private Long extractEventId(EventService eventService) {
        return eventService.getEvent().getId();
    }

    private EventService findEventServiceOrThrow(Long id) {
        return eventServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EVENT_SERVICE_NOT_FOUND_WITH_ID + id));
    }
}
