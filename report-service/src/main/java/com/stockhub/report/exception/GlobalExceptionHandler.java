package com.stockhub.report.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Report error - 400
    @ExceptionHandler(ReportException.class)
    public ResponseEntity<Map<String, Object>>
    handleReport(ReportException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, ex.getMessage()));
    }

    // Any other error - 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500,
                        "Something went wrong"));
    }
}