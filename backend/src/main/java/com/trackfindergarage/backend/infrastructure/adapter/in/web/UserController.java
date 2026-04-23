package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.PublicProfileUseCase;
import com.trackfindergarage.backend.application.port.in.UserUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.PublicUserProfileResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UpdateCurrentUserProfileRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.UserResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone la API HTTP relacionada con usuarios y perfiles de cuenta.
 *
 * <p>Combina operaciones de autoservicio para el usuario autenticado con operaciones administrativas de gestion global.</p>
 */
@RestController
@RequestMapping("/users")
public class UserController extends AbstractWebController {

    private final UserUseCase userUseCase;
    private final PublicProfileUseCase publicProfileUseCase;
    private final UserWebMapper userWebMapper;

    public UserController(UserUseCase userUseCase,
                          PublicProfileUseCase publicProfileUseCase,
                          UserWebMapper userWebMapper) {
        this.userUseCase = userUseCase;
        this.publicProfileUseCase = publicProfileUseCase;
        this.userWebMapper = userWebMapper;
    }

    /**
     * Recupera el perfil del usuario autenticado.
     *
     * @param authentication autenticación resuelta por Spring Security
     * @return usuario autenticado
     */
    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        return userWebMapper.toResponse(userUseCase.getCurrentUser(authenticatedEmail(authentication)));
    }

    /**
     * Recupera la información pública de un usuario a partir de su alias.
     *
     * @param displayName alias público del usuario
     * @return perfil público resumido
     */
    @GetMapping("/public/{displayName}")
    public PublicUserProfileResponse getPublicUserProfile(@PathVariable String displayName) {
        var publicProfile = publicProfileUseCase.getPublicUserProfile(displayName);

        return PublicUserProfileResponse.builder()
                .id(publicProfile.id())
                .displayName(publicProfile.displayName())
                .completedEvents(publicProfile.completedEvents())
                .visitedCircuits(publicProfile.visitedCircuits())
                .topFiveLapTimes(publicProfile.topFiveLapTimes())
                .poleCount(publicProfile.poleCount())
                .build();
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param authentication autenticación resuelta por Spring Security
     * @param request nuevos datos del perfil
     * @return usuario actualizado
     */
    @PutMapping("/me")
    public UserResponse updateCurrentUser(Authentication authentication,
                                          @Valid @RequestBody UpdateCurrentUserProfileRequest request) {
        return userWebMapper.toResponse(
                userUseCase.updateCurrentUserProfile(
                        authenticatedEmail(authentication),
                        request.getName(),
                        request.getSurname(),
                        request.getEmail(),
                        request.getAddress(),
                        request.getPhone(),
                        request.getPassword()
                )
        );
    }

    /**
     * Lista todos los usuarios.
     *
     * @return listado completo de usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return mapResponses(userUseCase.getAllUsers(), userWebMapper::toResponse);
    }

    /**
     * Habilita una cuenta de usuario.
     *
     * @param id identificador del usuario
     * @return usuario habilitado
     */
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse enableUser(@PathVariable Long id) {
        return userWebMapper.toResponse(userUseCase.enableUser(id));
    }

    /**
     * Deshabilita una cuenta de usuario.
     *
     * @param id identificador del usuario
     * @return usuario deshabilitado
     */
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse disableUser(@PathVariable Long id) {
        return userWebMapper.toResponse(userUseCase.disableUser(id));
    }
}
