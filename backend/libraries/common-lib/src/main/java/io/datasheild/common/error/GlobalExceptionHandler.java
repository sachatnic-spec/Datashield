package io.datasheild.common.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all DataShield services
 * Returns RFC 7807 Problem Details format
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DataShieldException.class)
    public ResponseEntity<ErrorResponse> handleDataShieldException(DataShieldException ex, WebRequest request) {
        log.error("DataShieldException: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .type("https://datashield.io/errors/" + ex.getErrorCode().toLowerCase().replace("_", "-"))
            .title(ex.getErrorCode())
            .status(ex.getHttpStatus())
            .detail(ex.getMessage())
            .instance(request.getDescription(false).replace("uri=", ""))
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getHttpStatus()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage(),
                (existing, replacement) -> existing
            ));
        
        ErrorResponse error = ErrorResponse.builder()
            .type("https://datashield.io/errors/validation-error")
            .title("VALIDATION_ERROR")
            .status(400)
            .detail("Validation failed: " + errors)
            .instance(request.getDescription(false).replace("uri=", ""))
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .type("https://datashield.io/errors/internal-error")
            .title("INTERNAL_SERVER_ERROR")
            .status(500)
            .detail("An unexpected error occurred")
            .instance(request.getDescription(false).replace("uri=", ""))
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
