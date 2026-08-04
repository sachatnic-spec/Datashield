package io.datasheild.tenantservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvisionTenantRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    private String executedBy;

    private Boolean waitForCompletion;

    @Builder.Default
    private Long timeoutSeconds = 300L;
}
