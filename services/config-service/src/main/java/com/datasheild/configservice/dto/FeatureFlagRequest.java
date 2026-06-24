package com.datasheild.configservice.dto;

import com.datasheild.configservice.entity.FeatureFlagName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeatureFlagRequest(
        @NotBlank String tenantId,
        @NotNull FeatureFlagName featureName,
        boolean enabled) {
}
