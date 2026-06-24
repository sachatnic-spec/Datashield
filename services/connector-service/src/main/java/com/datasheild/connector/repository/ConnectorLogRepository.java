package com.datasheild.connector.repository;

import com.datasheild.connector.entity.ConnectorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectorLogRepository extends JpaRepository<ConnectorLog, Long> {
    Page<ConnectorLog> findByConnectorIdOrderByLoggedAtDesc(Long connectorId, Pageable pageable);
}
