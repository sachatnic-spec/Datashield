package io.datasheild.tenantservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TenantException.class)
    public ResponseEntity<ErrorResponse> handleTenantException(TenantException ex, WebRequest request) {
        log.error("TenantException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("https://datashield.io/errors/tenant-error")
            .title(ex.getErrorCode())
            .status(ex.getHttpStatus())
            .detail(ex.getMessage())
            .instance(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getHttpStatus()));
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

        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("https://datashield.io/errors/validation-error")
            .title("VALIDATION_ERROR")
            .status(400)
            .detail("Validation failed")
            .instance(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .validationErrors(errors)
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .type("https://datashield.io/errors/internal-error")
            .title("INTERNAL_SERVER_ERROR")
            .status(500)
            .detail("An unexpected error occurred")
            .instance(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse {
        private String type;
        private String title;
        private int status;
        private String detail;
        private String instance;
        private LocalDateTime timestamp;
        private Map<String, String> validationErrors;
    }
}
