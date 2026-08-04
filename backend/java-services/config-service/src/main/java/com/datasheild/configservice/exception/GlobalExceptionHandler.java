package com.datasheild.configservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({IllegalArgumentException.class, CryptoOperationException.class})
    public ResponseEntity<ProblemDetail> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
        HttpStatus status = exception instanceof CryptoOperationException ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        String title = exception instanceof CryptoOperationException ? "Encryption error" : "Bad request";
        return response(status, title, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnhandled(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", exception.getMessage(), request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, "Request validation failed");
        problemDetail.setTitle("Validation failed");
        problemDetail.setProperty("timestamp", OffsetDateTime.now().toString());
        if (request instanceof ServletWebRequest servletWebRequest) {
            problemDetail.setProperty("path", servletWebRequest.getRequest().getRequestURI());
        }
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
                .toList();
        problemDetail.setProperty("errors", errors);
        return new ResponseEntity<>(problemDetail, headers, status);
    }

    private ResponseEntity<ProblemDetail> response(HttpStatus status, String title, String detail, String path) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", OffsetDateTime.now().toString());
        problemDetail.setProperty("path", path);
        return ResponseEntity.status(status).body(problemDetail);
    }
}
