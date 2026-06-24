package io.datasheild.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    @JsonProperty("event_id")
    private UUID eventId;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("channels_sent")
    private Integer channelsSent;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("recipient_count")
    private Integer recipientCount;
}
