package io.datasheild.auditservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("audit_event_id")
    private UUID auditEventId;

    @JsonProperty("event_summary")
    private String eventSummary;

    @JsonProperty("s3_object_key")
    private String s3ObjectKey;

    @JsonProperty("sha256_hash")
    private String sha256Hash;

    @JsonProperty("hash_chain_valid")
    private String hashChainValid;

    @JsonProperty("archived")
    private Boolean archived;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("archived_at")
    private LocalDateTime archivedAt;
}
