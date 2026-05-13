package com.stockhub.supplier.exception;

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

    // Helper to build error response map
    private Map<String, Object> buildError(
            int status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("message", message);
        error.put("timestamp",
                LocalDateTime.now().toString());
        return error;
    }

    // Handle supplier not found - 404
    @ExceptionHandler(SupplierNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleNotFound(SupplierNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildError(404, ex.getMessage()));
    }

    // Handle inactive supplier - 400
    @ExceptionHandler(SupplierInactiveException.class)
    public ResponseEntity<Map<String, Object>>
    handleInactive(SupplierInactiveException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, ex.getMessage()));
    }

    // Handle @Valid failures - 400
    @ExceptionHandler(
            MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidation(
            MethodArgumentNotValidException ex) {

        // Collect all field level errors
        Map<String, String> fieldErrors =
                new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String field =
                            ((FieldError) error)
                                    .getField();
                    String msg =
                            error.getDefaultMessage();
                    fieldErrors.put(field, msg);
                });

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "Validation failed");
        response.put("errors", fieldErrors);
        response.put("timestamp",
                LocalDateTime.now().toString());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Catch all other exceptions - 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500,
                        "Something went wrong"));
    }
}