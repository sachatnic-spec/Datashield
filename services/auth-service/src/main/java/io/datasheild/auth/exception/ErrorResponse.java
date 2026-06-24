package io.datasheild.auth.exception;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String type;           // e.g., "about:blank" or custom URI
    private String title;          // HTTP status text
    private int status;            // HTTP status code
    private String detail;         // Specific error description
    private String instance;       // Request identifier or path
    private LocalDateTime timestamp;
    private String traceId;        // For correlation
    
    @Singular
    private List<FieldError> invalidFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private String rejectedValue;
    }

    public static ErrorResponse of(String title, int status, String detail) {
        return ErrorResponse.builder()
                .title(title)
                .status(status)
                .detail(detail)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(String title, int status, String detail, String instance) {
        return ErrorResponse.builder()
                .title(title)
                .status(status)
                .detail(detail)
                .instance(instance)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
