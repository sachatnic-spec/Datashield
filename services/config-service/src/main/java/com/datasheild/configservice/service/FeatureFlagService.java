package com.datasheild.configservice.service;

import com.datasheild.configservice.dto.FeatureFlagRequest;
import com.datasheild.configservice.dto.FeatureFlagResponse;
import com.datasheild.configservice.entity.ConfigStatus;
import com.datasheild.configservice.entity.FeatureFlag;
import com.datasheild.configservice.entity.FeatureFlagName;
import com.datasheild.configservice.repository.FeatureFlagRepository;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    @Cacheable(cacheNames = "featureFlags", key = "#tenantId")
    public FeatureFlagResponse getFeatureFlags(String tenantId) {
        return buildResponse(tenantId);
    }

    @Transactional
    @CacheEvict(cacheNames = "featureFlags", key = "#request.tenantId")
    public FeatureFlagResponse upsertFeatureFlag(FeatureFlagRequest request) {
        FeatureFlag featureFlag = featureFlagRepository.findByTenantIdAndFeatureName(request.tenantId(), request.featureName())
                .orElseGet(() -> FeatureFlag.builder()
                        .tenantId(request.tenantId())
                        .featureName(request.featureName())
                        .build());
        featureFlag.setEnabled(request.enabled());
        featureFlag.setStatus(ConfigStatus.ACTIVE);
        featureFlagRepository.save(featureFlag);
        return buildResponse(request.tenantId());
    }

    private FeatureFlagResponse buildResponse(String tenantId) {
        Map<FeatureFlagName, Boolean> flags = new EnumMap<>(FeatureFlagName.class);
        Arrays.stream(FeatureFlagName.values()).forEach(flag -> flags.put(flag, Boolean.FALSE));
        featureFlagRepository.findByTenantIdAndStatus(tenantId, ConfigStatus.ACTIVE)
                .forEach(flag -> flags.put(flag.getFeatureName(), flag.isEnabled()));
        return new FeatureFlagResponse(tenantId, flags);
    }
}
