package io.datasheild.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerNotificationRequest {

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("recipients")
    private List<String> recipients;

    @JsonProperty("template_code")
    private String templateCode;

    @JsonProperty("language")
    private String language;

    @JsonProperty("variables")
    private java.util.Map<String, String> variables;

    @JsonProperty("channels")
    private List<String> channels;

    @JsonProperty("scheduled_for")
    private String scheduledFor;

    public boolean isValid() {
        return correlationId != null && !correlationId.isBlank() &&
               eventType != null && !eventType.isBlank() &&
               recipients != null && !recipients.isEmpty() &&
               templateCode != null && !templateCode.isBlank() &&
               channels != null && !channels.isEmpty();
    }
}
