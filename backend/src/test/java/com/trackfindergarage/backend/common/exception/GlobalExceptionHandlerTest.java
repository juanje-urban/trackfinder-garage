package com.trackfindergarage.backend.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundReturnsNotFoundResponse() {
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleNotFound(new ResourceNotFoundException("missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().get("error"));
    }

    @Test
    void handleDuplicateResourceReturnsConflictResponse() {
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleDuplicateResource(new DuplicateResourceException("duplicate"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("duplicate", response.getBody().get("error"));
    }

    @Test
    void handleConflictReturnsConflictResponse() {
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleConflict(new ConflictException("conflict"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("conflict", response.getBody().get("error"));
    }

    @Test
    void handleIllegalArgumentReturnsBadRequestResponse() {
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleIllegalArgument(new IllegalArgumentException("invalid"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid", response.getBody().get("error"));
    }

    @Test
    void handleValidationReturnsFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "name", "must not be blank"),
                new FieldError("request", "price", "must be greater than 0")
        ));

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must not be blank", response.getBody().get("name"));
        assertEquals("must be greater than 0", response.getBody().get("price"));
    }
}
