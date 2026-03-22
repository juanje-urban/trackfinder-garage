package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.LapTimeResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.LapTimeWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lap-times")
public class LapTimeController {

    private final LapTimeUseCase lapTimeUseCase;
    private final LapTimeWebMapper lapTimeWebMapper;

    public LapTimeController(LapTimeUseCase lapTimeUseCase, LapTimeWebMapper lapTimeWebMapper) {
        this.lapTimeUseCase = lapTimeUseCase;
        this.lapTimeWebMapper = lapTimeWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LapTimeResponse createLapTime(@Valid @RequestBody CreateLapTimeRequest request) {
        LapTime createdLapTime = lapTimeUseCase.createLapTime(lapTimeWebMapper.toDomain(request));
        return lapTimeWebMapper.toResponse(createdLapTime);
    }

    @PutMapping("/{id}")
    public LapTimeResponse updateLapTime(@PathVariable Long id,
                                         @Valid @RequestBody UpdateLapTimeRequest request) {
        LapTime lapTimeToUpdate = new LapTime();
        lapTimeWebMapper.updateDomain(lapTimeToUpdate, request);

        LapTime updatedLapTime = lapTimeUseCase.updateLapTime(id, lapTimeToUpdate);
        return lapTimeWebMapper.toResponse(updatedLapTime);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLapTime(@PathVariable Long id) {
        lapTimeUseCase.deleteLapTime(id);
    }

    @GetMapping
    public List<LapTimeResponse> getAllLapTimes() {
        return lapTimeUseCase.getAllLapTimes()
                .stream()
                .map(lapTimeWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public LapTimeResponse getLapTimeById(@PathVariable Long id) {
        return lapTimeWebMapper.toResponse(lapTimeUseCase.getLapTimeById(id));
    }

    @GetMapping("/user/{userId}")
    public List<LapTimeResponse> getLapTimesByUserId(@PathVariable Long userId) {
        return lapTimeUseCase.getLapTimesByUserId(userId)
                .stream()
                .map(lapTimeWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/track/{trackId}")
    public List<LapTimeResponse> getLapTimesByTrackId(@PathVariable Long trackId) {
        return lapTimeUseCase.getLapTimesByTrackId(trackId)
                .stream()
                .map(lapTimeWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/track/{trackId}/best")
    public LapTimeResponse getBestLapTimeByTrackId(@PathVariable Long trackId) {
        return lapTimeWebMapper.toResponse(lapTimeUseCase.getBestLapTimeByTrackId(trackId));
    }

    @GetMapping("/user/{userId}/track/{trackId}/best")
    public LapTimeResponse getBestLapTimeByUserIdAndTrackId(@PathVariable Long userId,
                                                            @PathVariable Long trackId) {
        return lapTimeWebMapper.toResponse(lapTimeUseCase.getBestLapTimeByUserIdAndTrackId(userId, trackId));
    }

    @GetMapping("/track/{trackId}/ranking")
    public List<LapTimeResponse> getRankingByTrackId(@PathVariable Long trackId) {
        return lapTimeUseCase.getRankingByTrackId(trackId)
                .stream()
                .map(lapTimeWebMapper::toResponse)
                .toList();
    }
}
