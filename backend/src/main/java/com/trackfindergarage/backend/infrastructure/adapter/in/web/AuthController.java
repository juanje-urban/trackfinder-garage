package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import com.trackfindergarage.backend.application.port.in.AuthUseCase;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthLoginRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthRegisterRequest;
import com.trackfindergarage.backend.infrastructure.adapter.in.web.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authUseCase.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        return authUseCase.register(
                request.getDisplayName(),
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getSurname(),
                request.getAddress(),
                request.getPhone()
        );
    }
}
