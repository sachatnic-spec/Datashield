package com.datasheild.siem.repository;

import com.datasheild.siem.entity.SiemAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiemAlertRepository extends JpaRepository<SiemAlert, Long> {
    Page<SiemAlert> findByTenantId(String tenantId, Pageable pageable);
}
