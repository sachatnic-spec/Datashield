package com.datasheild.configservice.repository;

import com.datasheild.configservice.entity.ApiKey;
import com.datasheild.configservice.entity.ConfigStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    List<ApiKey> findByTenantIdAndStatus(String tenantId, ConfigStatus status);
}
