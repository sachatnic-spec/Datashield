package io.datasheild.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DLQItemResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("event_id")
    private UUID eventId;

    @JsonProperty("failure_reason")
    private String failureReason;

    @JsonProperty("retry_count")
    private Integer retryCount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
