package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.TrackServiceUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateTrackServiceRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.TrackServiceResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.TrackServiceWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/track-services")
@PreAuthorize("hasRole('ADMIN')")
public class TrackServiceController extends AbstractWebController {

    private final TrackServiceUseCase trackServiceUseCase;
    private final TrackServiceWebMapper trackServiceWebMapper;

    public TrackServiceController(TrackServiceUseCase trackServiceUseCase,
                                  TrackServiceWebMapper trackServiceWebMapper) {
        this.trackServiceUseCase = trackServiceUseCase;
        this.trackServiceWebMapper = trackServiceWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackServiceResponse createTrackService(@Valid @RequestBody CreateTrackServiceRequest request) {
        return trackServiceWebMapper.toResponse(trackServiceUseCase.createTrackService(trackServiceWebMapper.toDomain(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrackService(@PathVariable Long id) {
        trackServiceUseCase.deleteTrackService(id);
    }

    @GetMapping
    public List<TrackServiceResponse> getAllTrackServices() {
        return mapResponses(trackServiceUseCase.getAllTrackServices(), trackServiceWebMapper::toResponse);
    }
}
