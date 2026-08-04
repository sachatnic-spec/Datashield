package io.datasheild.tenantservice.dto;

import io.datasheild.tenantservice.entity.Tenant;
import jakarta.validation.constraints.*;
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
public class CreateTenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(min = 3, max = 255, message = "Name must be 3-255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be <= 1000 characters")
    private String description;

    @NotNull(message = "Tier is required")
    private Tenant.TenantTier tier;

    @NotBlank(message = "Schema name is required")
    @Size(min = 3, max = 63, message = "Schema name must be 3-63 characters (PostgreSQL limit)")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Schema name must contain only lowercase letters, digits, and underscores")
    private String schemaName;

    @Email(message = "Support email must be valid")
    private String supportEmail;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Support phone must be valid E.164 format")
    private String supportPhone;

    @Email(message = "Invoice email must be valid")
    private String invoiceEmail;

    @NotNull(message = "Contract start date is required")
    @FutureOrPresent(message = "Contract start date must be today or in future")
    private LocalDateTime contractStartDate;

    @FutureOrPresent(message = "Contract end date must be today or in future")
    private LocalDateTime contractEndDate;

    @Builder.Default
    private Boolean autoRenewal = true;

    private String logoUrl;

    @Builder.Default
    @Min(value = 100, message = "Max data principals must be >= 100")
    private Long maxDataPrincipals = 10000L;

    @Builder.Default
    @Min(value = 100, message = "Max consents must be >= 100")
    private Long maxConsents = 100000L;

    @Builder.Default
    @Min(value = 10, message = "Max DPR requests must be >= 10")
    private Long maxDPRRequests = 50000L;

    @Builder.Default
    @Min(value = 10, message = "Max storage must be >= 10 GB")
    private Long maxStorageGB = 1000L;

    @Builder.Default
    @Min(value = 100, message = "API rate limit must be >= 100 RPM")
    private Integer apiRateLimitRPM = 10000;
}
