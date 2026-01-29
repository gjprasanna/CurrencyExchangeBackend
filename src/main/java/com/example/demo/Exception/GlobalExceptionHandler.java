package com.example.demo.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing // Keep first error for duplicate fields
                ));

        log.warn("Validation failed: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Validation failed", errors));
    }

    /**
     * Handle business logic errors (invalid arguments)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage(), null));
    }

    /**
     * Handle database constraint violations (unique keys, foreign keys, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("Data integrity violation", e);

        String message = "Database constraint violation";

        // Try to extract meaningful message
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Duplicate entry")) {
                message = "Duplicate entry: Record already exists";
            } else if (e.getMessage().contains("foreign key constraint")) {
                message = "Cannot delete: Record is referenced by other data";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(message, null));
    }

    /**
     * Handle concurrent modification errors
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleOptimisticLocking(ObjectOptimisticLockingFailureException e) {
        log.error("Optimistic locking failure", e);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(
                        "The record was modified by another user. Please refresh and try again.",
                        null
                ));
    }

    /**
     * Handle generic runtime errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        log.error("Runtime exception occurred", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(
                        "An unexpected error occurred. Please try again.",
                        Map.of("type", e.getClass().getSimpleName())
                ));
    }

    /**
     * Catch-all exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        // Log full stack trace for debugging
        log.error("Unhandled exception: {}", e.getClass().getName(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(
                        "An unexpected error occurred. Please contact support if the problem persists.",
                        Map.of(
                                "type", e.getClass().getSimpleName(),
                                "timestamp", LocalDateTime.now().toString()
                        )
                ));
    }

    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(String message, Map<String, ?> details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", LocalDateTime.now());

        if (details != null && !details.isEmpty()) {
            response.put("details", details);
        }

        return response;
    }
}