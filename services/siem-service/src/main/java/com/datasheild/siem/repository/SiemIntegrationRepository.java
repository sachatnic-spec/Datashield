package com.datasheild.siem.repository;

import com.datasheild.siem.entity.SiemIntegration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiemIntegrationRepository extends JpaRepository<SiemIntegration, Long> {
    List<SiemIntegration> findByTenantId(String tenantId);
}
