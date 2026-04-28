package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOwnLapTimeRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.LapTimeResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.LapTimeWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints HTTP relacionados con los tiempos de vuelta de los usuarios.
 */
@RestController
@RequestMapping("/lap-times")
public class LapTimeController extends AbstractWebController {

    private final LapTimeUseCase lapTimeUseCase;
    private final LapTimeWebMapper lapTimeWebMapper;

    public LapTimeController(LapTimeUseCase lapTimeUseCase, LapTimeWebMapper lapTimeWebMapper) {
        this.lapTimeUseCase = lapTimeUseCase;
        this.lapTimeWebMapper = lapTimeWebMapper;
    }

    /**
     * Registra un nuevo tiempo de vuelta para el usuario.
     *
     * @param request petición con los datos de la vuelta
     * @param authentication autenticación del usuario actual
     * @return respuesta con el tiempo creado
     */
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

    /**
     * Elimina un tiempo de vuelta del usuario.
     *
     * @param id identificador del tiempo de vuelta
     * @param authentication autenticación del usuario actual
     */
    @DeleteMapping("/me/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUserLapTime(@PathVariable Long id, Authentication authentication) {
        lapTimeUseCase.deleteOwnLapTime(authenticatedEmail(authentication), id);
    }

    /**
     * Recupera los tiempos de vuelta del usuario autenticado.
     *
     * @param authentication autenticación del usuario actual
     * @return listado de tiempos del usuario
     */
    @GetMapping("/me")
    public List<LapTimeResponse> getCurrentUserLapTimes(Authentication authentication) {
        return mapResponses(
                lapTimeUseCase.getLapTimesByAuthenticatedEmail(authenticatedEmail(authentication)),
                lapTimeWebMapper::toResponse
        );
    }

    /**
     * Recupera los tiempos de vuelta de un usuario concreto.
     *
     * @param userId identificador del usuario
     * @return listado de tiempos del usuario
     */
    @GetMapping("/user/{userId}")
    public List<LapTimeResponse> getLapTimesByUserId(@PathVariable Long userId) {
        return mapResponses(lapTimeUseCase.getLapTimesByUserId(userId), lapTimeWebMapper::toResponse);
    }
}
