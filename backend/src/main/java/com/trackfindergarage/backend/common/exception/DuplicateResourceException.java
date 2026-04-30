package com.trackfindergarage.backend.common.exception;

/**
 * Señala que se intenta crear o registrar un recurso que ya existe.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
