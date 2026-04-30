package com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper;

import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackRecordResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper web para proyectar tiempos de vuelta a vistas de récord de circuito.
 *
 * <p>Extrae de un {@link LapTime} únicamente los campos relevantes para representar una entrada de
 * ranking o récord de pista.</p>
 */
@Component
public class TrackRecordWebMapper {

    public TrackRecordResponse toResponse(LapTime lapTime) {
        return TrackRecordResponse.builder()
                .trackId(lapTime.getTrack() != null ? lapTime.getTrack().getId() : null)
                .trackName(lapTime.getTrack() != null ? lapTime.getTrack().getName() : null)
                .userDisplayName(lapTime.getUser() != null ? lapTime.getUser().getDisplayName() : null)
                .lapDate(lapTime.getLapDate())
                .lapTimeMs(lapTime.getLapTimeMs())
                .vehicle(lapTime.getVehicle())
                .build();
    }
}
