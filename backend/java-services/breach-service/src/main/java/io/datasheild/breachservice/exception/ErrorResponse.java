package io.datasheild.breachservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private String traceId;
}
