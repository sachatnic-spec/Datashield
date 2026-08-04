package io.datasheild.common.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * RFC 7807 Problem Details response format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private LocalDateTime timestamp;
    private String traceId;
    
    public static ErrorResponse of(String title, int status, String detail, String instance) {
        return ErrorResponse.builder()
            .type("https://datashield.io/errors/" + title.toLowerCase().replace("_", "-"))
            .title(title)
            .status(status)
            .detail(detail)
            .instance(instance)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static ErrorResponse of(String title, HttpStatus status, String detail, String instance) {
        return of(title, status.value(), detail, instance);
    }
}
