package io.datasheild.consentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String type;  // RFC 7807: problem type URI
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private String traceId;
}
