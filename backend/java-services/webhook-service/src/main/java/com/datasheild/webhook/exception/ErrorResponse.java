package com.datasheild.webhook.exception;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
}
