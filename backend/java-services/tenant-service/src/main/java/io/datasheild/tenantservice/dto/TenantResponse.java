package io.datasheild.tenantservice.dto;

import io.datasheild.tenantservice.entity.Tenant;
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
public class TenantResponse {

    private UUID id;

    private String name;

    private String description;

    private Tenant.TenantTier tier;

    private Tenant.SubscriptionStatus subscriptionStatus;

    private String schemaName;

    private Tenant.ProvisioningStatus provisioningStatus;

    private Long maxDataPrincipals;

    private Long maxConsents;

    private Long maxDPRRequests;

    private Long maxStorageGB;

    private Integer apiRateLimitRPM;

    private String logoUrl;

    private String supportEmail;

    private String supportPhone;

    private LocalDateTime contractStartDate;

    private LocalDateTime contractEndDate;

    private Boolean autoRenewal;

    private String invoiceEmail;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime archivedAt;

    public static TenantResponse fromEntity(Tenant tenant) {
        return TenantResponse.builder()
            .id(tenant.getId())
            .name(tenant.getName())
            .description(tenant.getDescription())
            .tier(tenant.getTier())
            .subscriptionStatus(tenant.getSubscriptionStatus())
            .schemaName(tenant.getSchemaName())
            .provisioningStatus(tenant.getProvisioningStatus())
            .maxDataPrincipals(tenant.getMaxDataPrincipals())
            .maxConsents(tenant.getMaxConsents())
            .maxDPRRequests(tenant.getMaxDPRRequests())
            .maxStorageGB(tenant.getMaxStorageGB())
            .apiRateLimitRPM(tenant.getApiRateLimitRPM())
            .logoUrl(tenant.getLogoUrl())
            .supportEmail(tenant.getSupportEmail())
            .supportPhone(tenant.getSupportPhone())
            .contractStartDate(tenant.getContractStartDate())
            .contractEndDate(tenant.getContractEndDate())
            .autoRenewal(tenant.getAutoRenewal())
            .invoiceEmail(tenant.getInvoiceEmail())
            .createdAt(tenant.getCreatedAt())
            .updatedAt(tenant.getUpdatedAt())
            .archivedAt(tenant.getArchivedAt())
            .build();
    }
}
