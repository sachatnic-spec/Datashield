package com.datasheild.connector.repository;

import com.datasheild.connector.entity.Connector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectorRepository extends JpaRepository<Connector, Long> {
    List<Connector> findByTenantId(String tenantId);
    List<Connector> findByTenantIdAndConnectorType(String tenantId, String connectorType);
    Optional<Connector> findByIdAndTenantId(Long id, String tenantId);
    Page<Connector> findByTenantId(String tenantId, Pageable pageable);
}
