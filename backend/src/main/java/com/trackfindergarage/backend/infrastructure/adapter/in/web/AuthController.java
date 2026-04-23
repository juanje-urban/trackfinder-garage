package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.AuthUseCase;
import com.trackfindergarage.backend.application.port.in.AuthRegistrationCommand;
import com.trackfindergarage.backend.application.port.in.OrganizerRegistrationCommand;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthLoginRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthRegisterRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.CreateOrganizerRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints de autenticacion de la API.
 *
 * <p>El sistema de login y sesión está basado en Basic Auth. No hay expiración de sesión y está altamente acoplado
 * al email y contraseña del usuario. Convendría estudiar el uso de JWT u otra alternativa.</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController extends AbstractWebController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    /**
     * Autentica a un usuario mediante su email y contraseña.
     *
     * <p>Valida la entrada gracias a {@code @Valid} y las restricciones incluidas en el DTO de entrada.</p>
     *
     * @param request credenciales enviadas desde el cliente
     * @return datos básicos de sesión del usuario autenticado
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authUseCase.login(request.getEmail(), request.getPassword());
    }

    /**
     * Devuelve la sesión actual del usuario autenticado.
     *
     * <p>Permite al frontend reconstruir su estado de sesión a partir del usuario autenticado por Spring Security.</p>
     *
     * @param authentication autenticación resuelta por Spring Security
     * @return información de la sesión activa
     */
    @GetMapping("/me")
    public AuthResponse getCurrentSession(Authentication authentication) {
        return authUseCase.getCurrentSession(authenticatedEmail(authentication));
    }

    /**
     * Registra un usuario de tipo {@code USER}.
     *
     * @param request datos de alta del usuario final
     * @return sesión inicial del usuario recién creado
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        return authUseCase.register(new AuthRegistrationCommand(
                request.getDisplayName(),
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getSurname(),
                request.getAddress(),
                request.getPhone()
        ));
    }

    /**
     * Registra una solicitud de cuenta de organizador.
     *
     * <p>La información común del usuario se encapsula en un {@link AuthRegistrationCommand} y los datos propios
     * del organizador se añaden en un {@link OrganizerRegistrationCommand}.</p>
     *
     * @param request datos de alta del organizador
     * @return sesión inicial asociada al usuario creado
     */
    @PostMapping("/register/organizer")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerOrganizer(@Valid @RequestBody CreateOrganizerRequest request) {
        return authUseCase.registerOrganizer(new OrganizerRegistrationCommand(
                new AuthRegistrationCommand(
                        request.getDisplayName(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getName(),
                        request.getSurname(),
                        request.getAddress(),
                        request.getPhone()
                ),
                request.getLegalName(),
                request.getCif()
        ));
    }
}
