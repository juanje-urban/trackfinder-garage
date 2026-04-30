package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.application.port.in.OrganizerEventDraft;
import com.trackfindergarage.backend.application.port.in.OrganizerEventServiceDraft;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceEventStatsView;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceSnapshot;
import com.trackfindergarage.backend.application.port.in.OrganizerWorkspaceStatsView;
import com.trackfindergarage.backend.domain.model.Event;
import com.trackfindergarage.backend.domain.model.EventService;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerManagedEventResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerWorkspaceEventStatsResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerWorkspaceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerWorkspaceStatsResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpsertOrganizerEventRequest;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapper web del workspace del organizador.
 *
 * <p>Transforma la vista agregada de aplicación del workspace en el DTO rico que consume el
 * frontend, y también convierte las peticiones de alta o edición de eventos en borradores de
 * aplicación.</p>
 */
@Component
public class OrganizerWorkspaceWebMapper {

    private final OrganizerWebMapper organizerWebMapper;
    private final ServiceWebMapper serviceWebMapper;
    private final OrganizerServiceWebMapper organizerServiceWebMapper;
    private final TrackWebMapper trackWebMapper;
    private final TrackServiceWebMapper trackServiceWebMapper;
    private final EventWebMapper eventWebMapper;
    private final EventServiceWebMapper eventServiceWebMapper;

    public OrganizerWorkspaceWebMapper(OrganizerWebMapper organizerWebMapper,
                                       ServiceWebMapper serviceWebMapper,
                                       OrganizerServiceWebMapper organizerServiceWebMapper,
                                       TrackWebMapper trackWebMapper,
                                       TrackServiceWebMapper trackServiceWebMapper,
                                       EventWebMapper eventWebMapper,
                                       EventServiceWebMapper eventServiceWebMapper) {
        this.organizerWebMapper = organizerWebMapper;
        this.serviceWebMapper = serviceWebMapper;
        this.organizerServiceWebMapper = organizerServiceWebMapper;
        this.trackWebMapper = trackWebMapper;
        this.trackServiceWebMapper = trackServiceWebMapper;
        this.eventWebMapper = eventWebMapper;
        this.eventServiceWebMapper = eventServiceWebMapper;
    }

    // Convierte la petición web en un borrador de aplicación, filtrando entradas nulas de servicios.
    public OrganizerEventDraft toDraft(UpsertOrganizerEventRequest request) {
        return new OrganizerEventDraft(
                request.getTrackId(),
                request.getEventDate(),
                request.getBasePrice(),
                request.getMaxParticipants(),
                request.getDescription(),
                (request.getServices() == null ? List.<com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.OrganizerEventServiceInput>of() : request.getServices()).stream()
                        .filter(Objects::nonNull)
                        .map(item -> new OrganizerEventServiceDraft(
                                item.getTrackServiceId(),
                                item.getOrganizerServiceId(),
                                item.getPrice()
                        ))
                        .toList()
        );
    }

    // Ensambla el DTO completo del workspace a partir de la vista agregada del caso de uso.
    public OrganizerWorkspaceResponse toResponse(OrganizerWorkspaceSnapshot snapshot) {
        Map<Long, OrganizerWorkspaceEventStatsView> eventStatsById = snapshot.stats().eventStats().stream()
                .collect(Collectors.toMap(OrganizerWorkspaceEventStatsView::eventId, Function.identity()));

        List<OrganizerManagedEventResponse> events = snapshot.events().stream()
                .map(event -> toManagedEventResponse(
                        event,
                        snapshot.eventServicesByEventId().getOrDefault(event.getId(), List.of()),
                        snapshot.bookedEventServiceIdsByEventId().getOrDefault(event.getId(), Set.of()),
                        eventStatsById.get(event.getId())
                ))
                .sorted(Comparator.comparing(item -> item.getEvent().getEventDate()))
                .toList();

        return OrganizerWorkspaceResponse.builder()
                .organizer(organizerWebMapper.toResponse(snapshot.organizer()))
                .availableServices(snapshot.availableServices().stream().map(serviceWebMapper::toResponse).toList())
                .organizerServices(snapshot.organizerServices().stream().map(organizerServiceWebMapper::toResponse).toList())
                .tracks(snapshot.tracks().stream().map(trackWebMapper::toResponse).toList())
                .trackServices(snapshot.trackServices().stream().map(trackServiceWebMapper::toResponse).toList())
                .events(events)
                .stats(toStatsResponse(snapshot.stats()))
                .build();
    }

    // Combina el evento, sus servicios y sus métricas en una fila gestionable por el organizador.
    private OrganizerManagedEventResponse toManagedEventResponse(Event event,
                                                                 List<EventService> services,
                                                                 Set<Long> bookedEventServiceIds,
                                                                 OrganizerWorkspaceEventStatsView stats) {
        return OrganizerManagedEventResponse.builder()
                .event(eventWebMapper.toResponse(event, stats != null ? stats.remainingCapacity() : 0))
                .services(services.stream()
                        .map(service -> eventServiceWebMapper.toResponse(
                                service,
                                bookedEventServiceIds.contains(service.getId())
                        ))
                        .toList())
                .stats(stats != null ? toEventStatsResponse(stats) : null)
                .build();
    }

    // Traduce las métricas agregadas del workspace al contrato web.
    private OrganizerWorkspaceStatsResponse toStatsResponse(OrganizerWorkspaceStatsView stats) {
        return OrganizerWorkspaceStatsResponse.builder()
                .totalBaseRevenue(stats.totalBaseRevenue())
                .totalServiceRevenue(stats.totalServiceRevenue())
                .totalGrossRevenue(stats.totalGrossRevenue())
                .totalBookings(stats.totalBookings())
                .totalSoldServices(stats.totalSoldServices())
                .futureEvents(stats.futureEvents())
                .pastEvents(stats.pastEvents())
                .totalCapacity(stats.totalCapacity())
                .totalRemainingCapacity(stats.totalRemainingCapacity())
                .eventStats(stats.eventStats().stream().map(this::toEventStatsResponse).toList())
                .build();
    }

    // Traduce las métricas específicas de un evento al contrato web.
    private OrganizerWorkspaceEventStatsResponse toEventStatsResponse(OrganizerWorkspaceEventStatsView stats) {
        return OrganizerWorkspaceEventStatsResponse.builder()
                .eventId(stats.eventId())
                .trackName(stats.trackName())
                .eventDate(stats.eventDate())
                .bookings(stats.bookings())
                .soldServices(stats.soldServices())
                .remainingCapacity(stats.remainingCapacity())
                .totalCapacity(stats.totalCapacity())
                .baseRevenue(stats.baseRevenue())
                .serviceRevenue(stats.serviceRevenue())
                .grossRevenue(stats.grossRevenue())
                .build();
    }
}
