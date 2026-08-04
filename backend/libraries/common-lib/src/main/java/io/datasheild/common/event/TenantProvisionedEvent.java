package io.datasheild.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantProvisionedEvent {
    private UUID tenantId;
    private String tenantName;
    private String tier;
    private String schemaName;
    private LocalDateTime provisionedAt;
    private String correlationId;
}
