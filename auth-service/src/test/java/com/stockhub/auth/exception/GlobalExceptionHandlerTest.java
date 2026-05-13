package com.stockhub.auth.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ─── UserAlreadyExists ─────────────────────

    @Test
    void handleUserAlreadyExists_returns409() {
        UserAlreadyExistsException ex =
                new UserAlreadyExistsException("Email already exists: test@test.com");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUserAlreadyExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("status", 409);
        assertThat(response.getBody()).containsEntry("message", "Email already exists: test@test.com");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    // ─── UserNotFound ──────────────────────────

    @Test
    void handleUserNotFound_returns404() {
        UserNotFoundException ex =
                new UserNotFoundException("User not found with id: 99");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUserNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody()).containsEntry("message", "User not found with id: 99");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    // ─── InvalidCredentials ────────────────────

    @Test
    void handleInvalidCredentials_returns401() {
        InvalidCredentialsException ex =
                new InvalidCredentialsException("Invalid email or password");

        ResponseEntity<Map<String, Object>> response =
                handler.handleInvalidCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("status", 401);
        assertThat(response.getBody()).containsEntry("message", "Invalid email or password");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    // ─── Validation Errors ─────────────────────

    @Test
    void handleValidationErrors_returns400WithFieldErrors() {
        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "registerRequest", "email", "Invalid email format");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("message", "Validation failed");
        assertThat(response.getBody()).containsKey("errors");
        assertThat(response.getBody()).containsKey("timestamp");

        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).containsEntry("email", "Invalid email format");
    }

    @Test
    void handleValidationErrors_multipleFieldErrors() {
        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError emailError = new FieldError("req", "email", "Email is required");
        FieldError passwordError = new FieldError("req", "password",
                "Password must be at least 6 characters");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(emailError, passwordError));

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidationErrors(ex);

        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).hasSize(2);
        assertThat(errors).containsKey("email");
        assertThat(errors).containsKey("password");
    }

    @Test
    void handleValidationErrors_emptyErrors_returnsEmptyMap() {
        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).isEmpty();
    }

    // ─── Generic Exception ─────────────────────

    @Test
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("Unexpected DB error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleGenericException(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody().get("message").toString())
                .contains("Something went wrong");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void handleGenericException_nullPointer_returns500() {
        Exception ex = new NullPointerException();

        ResponseEntity<Map<String, Object>> response =
                handler.handleGenericException(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
    }

    // ─── Timestamp present in all responses ────

    @Test
    void allHandlers_includeTimestamp() {
        assertThat(handler.handleUserNotFound(
                new UserNotFoundException("x")).getBody())
                .containsKey("timestamp");

        assertThat(handler.handleUserAlreadyExists(
                new UserAlreadyExistsException("x")).getBody())
                .containsKey("timestamp");

        assertThat(handler.handleInvalidCredentials(
                new InvalidCredentialsException("x")).getBody())
                .containsKey("timestamp");

        assertThat(handler.handleGenericException(
                new RuntimeException("x")).getBody())
                .containsKey("timestamp");
    }
}