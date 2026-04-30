package com.trackfindergarage.backend.common.exception;

/**
 * Señala un fallo de autenticación provocado por credenciales inválidas.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
