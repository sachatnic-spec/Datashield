package com.datasheild.configservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TenantConfigRequest(
        @NotBlank String consentModel,
        @NotNull @Min(1) Integer retentionPeriodDays,
        @NotBlank @Email String dpoEmail) {
}
