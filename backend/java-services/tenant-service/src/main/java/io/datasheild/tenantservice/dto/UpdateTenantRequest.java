package io.datasheild.tenantservice.dto;

import io.datasheild.tenantservice.entity.Tenant;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTenantRequest {

    @Size(min = 3, max = 255, message = "Name must be 3-255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be <= 1000 characters")
    private String description;

    @Email(message = "Support email must be valid")
    private String supportEmail;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Support phone must be valid E.164 format")
    private String supportPhone;

    @Email(message = "Invoice email must be valid")
    private String invoiceEmail;

    @FutureOrPresent(message = "Contract end date must be today or in future")
    private LocalDateTime contractEndDate;

    private Boolean autoRenewal;

    private String logoUrl;

    private Tenant.SubscriptionStatus subscriptionStatus;

    @Min(value = 100, message = "Max data principals must be >= 100")
    private Long maxDataPrincipals;

    @Min(value = 100, message = "Max consents must be >= 100")
    private Long maxConsents;

    @Min(value = 10, message = "Max DPR requests must be >= 10")
    private Long maxDPRRequests;

    @Min(value = 10, message = "Max storage must be >= 10 GB")
    private Long maxStorageGB;

    @Min(value = 100, message = "API rate limit must be >= 100 RPM")
    private Integer apiRateLimitRPM;
}
