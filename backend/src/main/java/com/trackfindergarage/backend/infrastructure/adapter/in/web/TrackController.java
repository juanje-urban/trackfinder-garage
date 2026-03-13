package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.TrackUseCase;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateTrackRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracks")
public class TrackController {

    private final TrackUseCase trackUseCase;
    private final TrackWebMapper trackWebMapper;

    public TrackController(TrackUseCase trackUseCase, TrackWebMapper trackWebMapper) {
        this.trackUseCase = trackUseCase;
        this.trackWebMapper = trackWebMapper;
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
}