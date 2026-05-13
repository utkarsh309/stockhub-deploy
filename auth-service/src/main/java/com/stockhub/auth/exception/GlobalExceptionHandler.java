package com.stockhub.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Helper method to build
    //     error response ───────────────────────
    private Map<String, Object> buildError(
            int status, String message) {

        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("message", message);
        error.put("timestamp",
                LocalDateTime.now().toString());
        return error;
    }

    // ─── Handle User Already Exists ────────────
    // Thrown when: registering with
    // existing email
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>>
    handleUserAlreadyExists(
            UserAlreadyExistsException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(buildError(409, ex.getMessage()));
    }

    // ─── Handle User Not Found ─────────────────
    // Thrown when: user id or email
    // does not exist
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleUserNotFound(
            UserNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404
                .body(buildError(404, ex.getMessage()));
    }

    // ─── Handle Invalid Credentials ────────────
    // Thrown when: wrong email or password
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>>
    handleInvalidCredentials(
            InvalidCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(buildError(401, ex.getMessage()));
    }

    // ─── Handle Validation Errors ──────────────
    // Thrown when: @Valid fails on
    // RequestBody fields
    // Example: blank email, short password
    @ExceptionHandler(
            MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Collect all field errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error)
                            .getField();
                    String errorMessage = error
                            .getDefaultMessage();
                    fieldErrors.put(fieldName,
                            errorMessage);
                });

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "Validation failed");
        response.put("errors", fieldErrors);
        response.put("timestamp",
                LocalDateTime.now().toString());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(response);
    }

    // ─── Handle Any Other Exception ────────────
    // Safety net for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGenericException(Exception ex) {

        return ResponseEntity
                .status(HttpStatus
                        .INTERNAL_SERVER_ERROR) // 500
                .body(buildError(500,
                        "Something went wrong." +
                                " Please try again."));
    }
}