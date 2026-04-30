package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.TrackServiceUseCase;
import com.trackfindergarage.backend.application.port.out.ServicePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackServicePersistencePort;
import com.trackfindergarage.backend.common.exception.DuplicateResourceException;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.Service;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.TrackService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementa la lógica de asignación de servicios a circuitos.
 *
 * <p>Se asegura de que la combinación del circuito-servicio no se repita y de que el servicio elegido esté
 * permitido para pistas antes de persistir la relación.</p>
 */
@org.springframework.stereotype.Service
@Transactional
public class TrackServiceService implements TrackServiceUseCase {

    private static final String TRACK_NOT_FOUND_WITH_ID = "Circuito no encontrado con id: ";
    private static final String SERVICE_NOT_FOUND_WITH_ID = "Servicio no encontrado con id: ";
    private static final String TRACK_SERVICE_NOT_FOUND_WITH_ID = "Servicio de circuito no encontrado con id: ";
    private static final String TRACK_ID_REQUIRED = "El id del circuito es obligatorio";
    private static final String SERVICE_ID_REQUIRED = "El id del servicio es obligatorio";
    private static final String TRACK_SERVICE_ALREADY_EXISTS =
            "Ya existe un servicio de circuito para el circuito con id %d y el servicio con id %d";
    private static final String SERVICE_NOT_ALLOWED_FOR_TRACKS =
            "El servicio con id %d no está permitido para circuitos";

    private final TrackServicePersistencePort trackServicePersistencePort;
    private final TrackPersistencePort trackPersistencePort;
    private final ServicePersistencePort servicePersistencePort;

    public TrackServiceService(TrackServicePersistencePort trackServicePersistencePort,
                               TrackPersistencePort trackPersistencePort,
                               ServicePersistencePort servicePersistencePort) {
        this.trackServicePersistencePort = trackServicePersistencePort;
        this.trackPersistencePort = trackPersistencePort;
        this.servicePersistencePort = servicePersistencePort;
    }

    /**
     * Crea una nueva relación de servicio para un circuito.
     *
     * @param trackService relación a crear
     * @return asignación persistida
     */
    @Override
    public TrackService createTrackService(TrackService trackService) {
        Long trackId = extractTrackId(trackService);
        Long serviceId = extractServiceId(trackService);

        ensureTrackServiceDoesNotExist(trackId, serviceId);
        Track track = loadTrack(trackId);
        Service service = loadTrackAllowedService(serviceId);

        trackService.setTrack(track);
        trackService.setService(service);
        return trackServicePersistencePort.save(trackService);
    }

    /**
     * Elimina una relación existente entre circuito y servicio.
     *
     * @param id identificador de la relación
     */
    @Override
    public void deleteTrackService(Long id) {
        trackServicePersistencePort.delete(findTrackServiceOrThrow(id));
    }

    /**
     * Recupera todas las asignaciones entre circuitos y servicios.
     *
     * @return listado de asignaciones
     */
    @Override
    @Transactional(readOnly = true)
    public List<TrackService> getAllTrackServices() {
        return trackServicePersistencePort.findAll();
    }

    //Métodos auxiliares para recuperar circuitos y servicios
    // Evita duplicar una misma combinación circuito-servicio.
    private void ensureTrackServiceDoesNotExist(Long trackId, Long serviceId) {
        trackServicePersistencePort.findByTrackIdAndServiceId(trackId, serviceId)
                .ifPresent(existingAssignment -> {
                    throw new DuplicateResourceException(TRACK_SERVICE_ALREADY_EXISTS.formatted(trackId, serviceId));
                });
    }

    // Carga el circuito destino de la asignación.
    private Track loadTrack(Long trackId) {
        return trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));
    }

    // Carga el servicio base desde el catálogo.
    private Service loadService(Long serviceId) {
        return servicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(SERVICE_NOT_FOUND_WITH_ID + serviceId));
    }

    // Carga el servicio y comprueba que pueda asociarse a circuitos.
    private Service loadTrackAllowedService(Long serviceId) {
        Service service = loadService(serviceId);

        if (!Boolean.TRUE.equals(service.getAllowedForTrack())) {
            throw new IllegalArgumentException(SERVICE_NOT_ALLOWED_FOR_TRACKS.formatted(service.getId()));
        }

        return service;
    }

    // Extrae y valida el id del circuito recibido.
    private Long extractTrackId(TrackService trackService) {
        if (trackService.getTrack() == null || trackService.getTrack().getId() == null) {
            throw new IllegalArgumentException(TRACK_ID_REQUIRED);
        }
        return trackService.getTrack().getId();
    }

    // Extrae y valida el id del servicio recibido.
    private Long extractServiceId(TrackService trackService) {
        if (trackService.getService() == null || trackService.getService().getId() == null) {
            throw new IllegalArgumentException(SERVICE_ID_REQUIRED);
        }
        return trackService.getService().getId();
    }

    // Carga una asignación circuito-servicio o centraliza el error de no encontrado.
    private TrackService findTrackServiceOrThrow(Long id) {
        return trackServicePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_SERVICE_NOT_FOUND_WITH_ID + id));
    }
}
