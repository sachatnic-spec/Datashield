package io.datasheild.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event schemas for DataShield services
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentGrantedEvent {
    private UUID consentRecordId;
    private UUID dataPrincipalId;
    private String purpose;
    private LocalDateTime grantedAt;
    private String source;
    private String correlationId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DPRSubmittedEvent {
    private UUID dprRequestId;
    private UUID dataPrincipalId;
    private String requestType;
    private LocalDateTime submittedAt;
    private String correlationId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreachReportedEvent {
    private UUID breachIncidentId;
    private Integer affectedRecords;
    private String dataType;
    private LocalDateTime discoveredAt;
    private String correlationId;
}

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyActivatedEvent {
    private UUID policyId;
    private String policyName;
    private String category;
    private LocalDateTime activatedAt;
    private String correlationId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorOnboardedEvent {
    private UUID vendorId;
    private String vendorName;
    private String vendorType;
    private LocalDateTime onboardedAt;
    private String correlationId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowCompletedEvent {
    private UUID workflowId;
    private String workflowType;
    private String status;
    private LocalDateTime completedAt;
    private String correlationId;
}
