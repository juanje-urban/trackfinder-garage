package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.function.Function;

/**
 * Clase base con utilidades comunes para controladores web.
 *
 * <p>Reúne helpers pequeños para extraer el correo autenticado y transformar colecciones de dominio
 * en respuestas HTTP sin repetir código en cada controlador.</p>
 */
abstract class AbstractWebController {

    protected String authenticatedEmail(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    protected <T, R> List<R> mapResponses(List<T> items, Function<T, R> mapper) {
        return items.stream()
                .map(mapper)
                .toList();
    }
}
