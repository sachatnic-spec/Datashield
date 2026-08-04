package com.datasheild.configservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.datasheild.configservice.dto.FeatureFlagResponse;
import com.datasheild.configservice.entity.ConfigStatus;
import com.datasheild.configservice.entity.FeatureFlag;
import com.datasheild.configservice.entity.FeatureFlagName;
import com.datasheild.configservice.repository.FeatureFlagRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagRepository featureFlagRepository;

    @InjectMocks
    private FeatureFlagService featureFlagService;

    @Test
    void shouldReturnDefaultsAndPersistedFlags() {
        when(featureFlagRepository.findByTenantIdAndStatus("tenant-a", ConfigStatus.ACTIVE)).thenReturn(List.of(
                FeatureFlag.builder().tenantId("tenant-a").featureName(FeatureFlagName.REDIS_ENABLED).enabled(true).build()));

        FeatureFlagResponse response = featureFlagService.getFeatureFlags("tenant-a");

        assertThat(response.flags()).containsEntry(FeatureFlagName.REDIS_ENABLED, true);
        assertThat(response.flags()).containsEntry(FeatureFlagName.ML_ENABLED, false);
        assertThat(response.flags()).containsEntry(FeatureFlagName.SIEM_ENABLED, false);
    }
}
