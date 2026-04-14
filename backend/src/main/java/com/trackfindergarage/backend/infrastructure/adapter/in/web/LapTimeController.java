package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOwnLapTimeRequest;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.LapTimeResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.LapTimeWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lap-times")
public class LapTimeController extends AbstractWebController {

    private final LapTimeUseCase lapTimeUseCase;
    private final LapTimeWebMapper lapTimeWebMapper;

    public LapTimeController(LapTimeUseCase lapTimeUseCase, LapTimeWebMapper lapTimeWebMapper) {
        this.lapTimeUseCase = lapTimeUseCase;
        this.lapTimeWebMapper = lapTimeWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LapTimeResponse createLapTime(@Valid @RequestBody CreateLapTimeRequest request) {
        return lapTimeWebMapper.toResponse(lapTimeUseCase.createLapTime(lapTimeWebMapper.toDomain(request)));
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public LapTimeResponse createCurrentUserLapTime(@Valid @RequestBody CreateOwnLapTimeRequest request,
                                                    Authentication authentication) {
        return lapTimeWebMapper.toResponse(
                lapTimeUseCase.createLapTimeForAuthenticatedUser(
                        authenticatedEmail(authentication),
                        request.getTrackId(),
                        request.getLapDate(),
                        request.getLapTimeMs(),
                        request.getVehicle()
                )
        );
    }

    @PutMapping("/{id}")
    public LapTimeResponse updateLapTime(@PathVariable Long id,
                                         @Valid @RequestBody UpdateLapTimeRequest request) {
        LapTime lapTimeToUpdate = new LapTime();
        lapTimeWebMapper.updateDomain(lapTimeToUpdate, request);
        return lapTimeWebMapper.toResponse(lapTimeUseCase.updateLapTime(id, lapTimeToUpdate));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLapTime(@PathVariable Long id) {
        lapTimeUseCase.deleteLapTime(id);
    }

    @DeleteMapping("/me/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUserLapTime(@PathVariable Long id, Authentication authentication) {
        lapTimeUseCase.deleteOwnLapTime(authenticatedEmail(authentication), id);
    }

    @GetMapping
    public List<LapTimeResponse> getAllLapTimes() {
        return mapResponses(lapTimeUseCase.getAllLapTimes(), lapTimeWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    public LapTimeResponse getLapTimeById(@PathVariable Long id) {
        return lapTimeWebMapper.toResponse(lapTimeUseCase.getLapTimeById(id));
    }

    @GetMapping("/me")
    public List<LapTimeResponse> getCurrentUserLapTimes(Authentication authentication) {
        return mapResponses(
                lapTimeUseCase.getLapTimesByAuthenticatedEmail(authenticatedEmail(authentication)),
                lapTimeWebMapper::toResponse
        );
    }

    @GetMapping("/user/{userId}")
    public List<LapTimeResponse> getLapTimesByUserId(@PathVariable Long userId) {
        return mapResponses(lapTimeUseCase.getLapTimesByUserId(userId), lapTimeWebMapper::toResponse);
    }

    @GetMapping("/track/{trackId}")
    public List<LapTimeResponse> getLapTimesByTrackId(@PathVariable Long trackId) {
        return mapResponses(lapTimeUseCase.getLapTimesByTrackId(trackId), lapTimeWebMapper::toResponse);
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
        return mapResponses(lapTimeUseCase.getRankingByTrackId(trackId), lapTimeWebMapper::toResponse);
    }
}
