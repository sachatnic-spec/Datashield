package com.datasheild.configservice.repository;

import com.datasheild.configservice.entity.ConfigStatus;
import com.datasheild.configservice.entity.TenantConfig;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantConfigRepository extends JpaRepository<TenantConfig, UUID> {

    Optional<TenantConfig> findByTenantIdAndStatus(String tenantId, ConfigStatus status);

    Optional<TenantConfig> findByTenantId(String tenantId);
}
