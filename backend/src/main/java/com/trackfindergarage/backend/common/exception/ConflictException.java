package com.trackfindergarage.backend.common.exception;

/**
 * Señala un conflicto de negocio entre el estado actual del sistema y la operación solicitada.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
