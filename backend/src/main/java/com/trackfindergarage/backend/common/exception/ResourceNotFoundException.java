package com.trackfindergarage.backend.common.exception;

/**
 * Señala que un recurso requerido por la operación no existe.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
