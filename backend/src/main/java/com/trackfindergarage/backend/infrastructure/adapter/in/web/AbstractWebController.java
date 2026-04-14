package com.trackfindergarage.backend.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.function.Function;

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
