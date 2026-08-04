package com.datasheild.configservice.repository;

import com.datasheild.configservice.entity.ConfigStatus;
import com.datasheild.configservice.entity.FeatureFlag;
import com.datasheild.configservice.entity.FeatureFlagName;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

    List<FeatureFlag> findByTenantIdAndStatus(String tenantId, ConfigStatus status);

    Optional<FeatureFlag> findByTenantIdAndFeatureName(String tenantId, FeatureFlagName featureName);
}
