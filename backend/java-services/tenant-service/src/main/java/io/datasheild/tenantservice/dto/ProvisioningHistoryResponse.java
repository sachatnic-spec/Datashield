package io.datasheild.tenantservice.dto;

import io.datasheild.tenantservice.entity.TenantProvisioningHistory;
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
public class ProvisioningHistoryResponse {

    private UUID id;

    private UUID tenantId;

    private String status;

    private String action;

    private String details;

    private String errorMessage;

    private String stackTrace;

    private String executedBy;

    private Long durationMs;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ProvisioningHistoryResponse fromEntity(TenantProvisioningHistory history) {
        return ProvisioningHistoryResponse.builder()
            .id(history.getId())
            .tenantId(history.getTenantId())
            .status(history.getStatus().name())
            .action(history.getAction())
            .details(history.getDetails())
            .errorMessage(history.getErrorMessage())
            .stackTrace(history.getStackTrace())
            .executedBy(history.getExecutedBy())
            .durationMs(history.getDurationMs())
            .createdAt(history.getCreatedAt())
            .updatedAt(history.getUpdatedAt())
            .build();
    }
}
