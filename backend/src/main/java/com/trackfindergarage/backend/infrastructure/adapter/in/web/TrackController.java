package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.application.port.in.TrackUseCase;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackRecordResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackRecordWebMapper;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracks")
public class TrackController {

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackResponse createTrack(@Valid @RequestBody CreateTrackRequest request) {
        Track createdTrack = trackUseCase.createTrack(trackWebMapper.toDomain(request));
        return trackWebMapper.toResponse(createdTrack);
    }

    @PutMapping("/{id}")
    public TrackResponse updateTrack(@PathVariable Long id,
                                     @Valid @RequestBody UpdateTrackRequest request) {
        Track trackToUpdate = new Track();
        trackWebMapper.updateDomain(trackToUpdate, request);

        Track updatedTrack = trackUseCase.updateTrack(id, trackToUpdate);
        return trackWebMapper.toResponse(updatedTrack);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrack(@PathVariable Long id) {
        trackUseCase.deleteTrack(id);
    }

    @GetMapping
    public List<TrackResponse> getAllTracks() {
        return trackUseCase.getAllTracks()
                .stream()
                .map(trackWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TrackResponse getTrackById(@PathVariable Long id) {
        return trackWebMapper.toResponse(trackUseCase.getTrackById(id));
    }

    @GetMapping("/{id}/record")
    public TrackRecordResponse getTrackRecord(@PathVariable Long id) {
        LapTime bestLapTime = lapTimeUseCase.getBestLapTimeByTrackId(id);
        return trackRecordWebMapper.toResponse(bestLapTime);
    }

    @GetMapping("/{id}/ranking")
    public List<TrackRecordResponse> getTrackRanking(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "3") int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        return lapTimeUseCase.getRankingByTrackId(id)
                .stream()
                .limit(limit)
                .map(trackRecordWebMapper::toResponse)
                .toList();
    }
}
