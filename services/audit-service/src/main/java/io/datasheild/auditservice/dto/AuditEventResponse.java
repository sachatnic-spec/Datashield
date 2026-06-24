package io.datasheild.auditservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("source_service")
    private String sourceService;

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("entity_id")
    private UUID entityId;

    @JsonProperty("actor_id")
    private String actorId;

    @JsonProperty("actor_role")
    private String actorRole;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("previous_state")
    private String previousState;

    @JsonProperty("current_state")
    private String currentState;
}
