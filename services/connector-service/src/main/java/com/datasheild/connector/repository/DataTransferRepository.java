package com.datasheild.connector.repository;

import com.datasheild.connector.entity.DataTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataTransferRepository extends JpaRepository<DataTransfer, Long> {
    List<DataTransfer> findByConnectorIdOrderByStartedAtDesc(Long connectorId);
    Page<DataTransfer> findByTenantId(String tenantId, Pageable pageable);
}
