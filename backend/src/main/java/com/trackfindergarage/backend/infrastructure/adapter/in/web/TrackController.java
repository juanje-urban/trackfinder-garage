package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.application.port.in.TrackUseCase;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackRecordResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackRecordWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints HTTP relacionados con el catálogo de circuitos.
 *
 * <p>Combina operaciones administrativas de alta y edición con consultas públicas y con la consulta
 * del ranking de tiempos por circuito.</p>
 */
@RestController
@RequestMapping("/tracks")
public class TrackController extends AbstractWebController {

    private final TrackUseCase trackUseCase;
    private final LapTimeUseCase lapTimeUseCase;
    private final TrackWebMapper trackWebMapper;
    private final TrackRecordWebMapper trackRecordWebMapper;

    public TrackController(TrackUseCase trackUseCase,
                           LapTimeUseCase lapTimeUseCase,
                           TrackWebMapper trackWebMapper,
                           TrackRecordWebMapper trackRecordWebMapper) {
        this.trackUseCase = trackUseCase;
        this.lapTimeUseCase = lapTimeUseCase;
        this.trackWebMapper = trackWebMapper;
        this.trackRecordWebMapper = trackRecordWebMapper;
    }

    /**
     * Crea un nuevo circuito (solo administrador).
     *
     * @param request datos del circuito a crear
     * @return circuito creado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TrackResponse createTrack(@Valid @RequestBody CreateTrackRequest request) {
        return trackWebMapper.toResponse(trackUseCase.createTrack(trackWebMapper.toDomain(request)));
    }

    /**
     * Actualiza un circuito existente.
     *
     * @param id identificador del circuito
     * @param request nuevos datos del circuito
     * @return circuito actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TrackResponse updateTrack(@PathVariable Long id,
                                     @Valid @RequestBody UpdateTrackRequest request) {
        Track trackToUpdate = new Track();
        trackWebMapper.updateDomain(trackToUpdate, request);
        return trackWebMapper.toResponse(trackUseCase.updateTrack(id, trackToUpdate));
    }

    /**
     * Recupera todos los circuitos del catálogo.
     *
     * @return listado de circuitos
     */
    @GetMapping
    public List<TrackResponse> getAllTracks() {
        return mapResponses(trackUseCase.getAllTracks(), trackWebMapper::toResponse);
    }

    /**
     * Recupera un circuito concreto por su identificador.
     *
     * @param id identificador del circuito
     * @return circuito encontrado
     */
    @GetMapping("/{id}")
    public TrackResponse getTrackById(@PathVariable Long id) {
        return trackWebMapper.toResponse(trackUseCase.getTrackById(id));
    }

    /**
     * Recupera el ranking de vueltas para un circuito, limitado a los primeros resultados.
     *
     * @param id identificador del circuito
     * @param limit numero máximo de registros a devolver
     * @return ranking resumido del circuito
     */
    @GetMapping("/{id}/ranking")
    public List<TrackRecordResponse> getTrackRanking(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "3") int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        return mapResponses(lapTimeUseCase.getRankingByTrackId(id)
                .stream()
                .limit(limit)
                .toList(), trackRecordWebMapper::toResponse);
    }
}
