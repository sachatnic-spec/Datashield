package com.datasheild.configservice.controller;

import com.datasheild.configservice.dto.FeatureFlagRequest;
import com.datasheild.configservice.dto.FeatureFlagResponse;
import com.datasheild.configservice.service.FeatureFlagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feature-flags")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @GetMapping("/{tenantId}")
    public FeatureFlagResponse getFeatureFlags(@PathVariable String tenantId) {
        return featureFlagService.getFeatureFlags(tenantId);
    }

    @PostMapping
    public FeatureFlagResponse upsertFeatureFlag(@Valid @RequestBody FeatureFlagRequest request) {
        return featureFlagService.upsertFeatureFlag(request);
    }
}
