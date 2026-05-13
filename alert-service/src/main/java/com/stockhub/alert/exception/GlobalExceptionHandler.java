package com.stockhub.alert.exception;

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

    // Build standard error response
    private Map<String, Object> buildError(
            int status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("message", message);
        error.put("timestamp",
                LocalDateTime.now().toString());
        return error;
    }

    // Alert not found - 404
    @ExceptionHandler(AlertNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleNotFound(AlertNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildError(404, ex.getMessage()));
    }

    // Validation errors - 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String field = ((FieldError) error).getField();
                    fieldErrors.put(field, error.getDefaultMessage());
                });
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "Validation failed");
        response.put("errors", fieldErrors);
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Any other error - 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500, "Something went wrong"));
    }
}