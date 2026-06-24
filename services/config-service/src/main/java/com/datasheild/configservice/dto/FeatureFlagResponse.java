package com.datasheild.configservice.dto;

import com.datasheild.configservice.entity.FeatureFlagName;
import java.util.Map;

public record FeatureFlagResponse(String tenantId, Map<FeatureFlagName, Boolean> flags) {
}
