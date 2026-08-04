package io.datasheild.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        String traceId = UUID.randomUUID().toString();

        ErrorResponse response = ErrorResponse.builder()
                .title(status.getReasonPhrase())
                .status(ex.getStatusCode())
                .detail(ex.getMessage())
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();

        log.warn("[{}] API Exception: {} - {}", traceId, status, ex.getMessage());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String traceId = UUID.randomUUID().toString();

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                fieldErrors.add(ErrorResponse.FieldError.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .rejectedValue(String.valueOf(fieldError.getRejectedValue()))
                        .build());
            }
        });

        ErrorResponse response = ErrorResponse.builder()
                .title("Validation Error")
                .status(status.value())
                .detail("Input validation failed")
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .invalidFields(fieldErrors)
                .build();

        log.warn("[{}] Validation Error: {} field(s) failed validation", traceId, fieldErrors.size());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String traceId = UUID.randomUUID().toString();

        ErrorResponse response = ErrorResponse.builder()
                .title("Internal Server Error")
                .status(status.value())
                .detail("An unexpected error occurred. Please contact support.")
                .instance(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .traceId(traceId)
                .build();

        log.error("[{}] Unexpected Exception: {}", traceId, ex.getMessage(), ex);
        return new ResponseEntity<>(response, status);
    }
}
