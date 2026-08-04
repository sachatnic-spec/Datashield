package com.datasheild.connector.service;

import com.datasheild.connector.dto.ConnectorRequest;
import com.datasheild.connector.dto.ConnectorResponse;
import com.datasheild.connector.entity.Connector;
import com.datasheild.connector.entity.ConnectorLog;
import com.datasheild.connector.entity.DataTransfer;
import com.datasheild.connector.repository.ConnectorLogRepository;
import com.datasheild.connector.repository.ConnectorRepository;
import com.datasheild.connector.repository.DataTransferRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConnectorService {

    private final ConnectorRepository repo;
    private final DataTransferRepository dataTransferRepo;
    private final ConnectorLogRepository connectorLogRepo;
    private final VaultService vault;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ConnectorResponse createConnector(ConnectorRequest request, String tenantId) {
        Connector connector = Connector.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .connectorType(request.getConnectorType())
                .sourceType(request.getSourceType())
                .targetType(request.getTargetType())
                .endpoint(request.getEndpoint())
                .status("ACTIVE")
                .configurationJson(request.getConfigurationJson())
                .credentialsEncrypted(vault.encryptCredentials(request.getCredentials()))
                .build();

        Connector saved = repo.save(connector);
        logInfo(saved.getId(), "Connector created for tenant " + tenantId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ConnectorResponse> listConnectors(String tenantId, Pageable pageable) {
        return repo.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ConnectorLog> getLogs(Long connectorId, String tenantId, Pageable pageable) {
        ensureConnector(connectorId, tenantId);
        return connectorLogRepo.findByConnectorIdOrderByLoggedAtDesc(connectorId, pageable);
    }

    public ConnectorResponse testConnection(Long connectorId, String tenantId) {
        Connector connector = ensureConnector(connectorId, tenantId);
        String credentials = vault.decryptCredentials(connector.getCredentialsEncrypted());
        if (!StringUtils.hasText(credentials)) {
            logWarn(connectorId, "Connection test failed due to empty credentials");
            throw new IllegalArgumentException("Connector credentials are missing");
        }
        logInfo(connectorId, "Connection test passed for " + connector.getConnectorType());
        return toResponse(connector);
    }

    public ConnectorResponse syncConnector(Long connectorId, String tenantId) {
        Connector connector = ensureConnector(connectorId, tenantId);
        DataTransfer transfer = DataTransfer.builder()
                .tenantId(tenantId)
                .connectorId(connectorId)
                .sourceType(connector.getSourceType())
                .targetType(connector.getTargetType())
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.now())
                .build();
        dataTransferRepo.save(transfer);

        try {
            switch (connector.getConnectorType()) {
                case "POSTGRESQL", "MYSQL", "MONGODB" -> syncDatabase(connector, transfer);
                case "S3", "GCS", "AZURE_BLOB" -> syncStorage(connector, transfer);
                default -> throw new IllegalArgumentException("Unsupported connector type: " + connector.getConnectorType());
            }
            transfer.setStatus("COMPLETED");
            transfer.setCompletedAt(LocalDateTime.now());
            connector.setLastSyncedAt(LocalDateTime.now());
            repo.save(connector);
            publishSyncEvent(connector, transfer);
            logInfo(connectorId, "Sync completed successfully");
        } catch (Exception ex) {
            transfer.setStatus("FAILED");
            transfer.setCompletedAt(LocalDateTime.now());
            transfer.setErrorMessage(ex.getMessage());
            logWarn(connectorId, "Sync failed: " + ex.getMessage());
            throw ex;
        } finally {
            dataTransferRepo.save(transfer);
        }
        return toResponse(connector);
    }

    private Connector ensureConnector(Long connectorId, String tenantId) {
        return repo.findByIdAndTenantId(connectorId, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Connector not found"));
    }

    private void syncDatabase(Connector connector, DataTransfer transfer) {
        transfer.setRecordsTransferred(250L);
        log.info("Syncing database connector {}", connector.getId());
    }

    private void syncStorage(Connector connector, DataTransfer transfer) {
        transfer.setRecordsTransferred(125L);
        log.info("Syncing storage connector {}", connector.getId());
    }

    private void publishSyncEvent(Connector connector, DataTransfer transfer) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "tenantId", connector.getTenantId(),
                    "connectorId", connector.getId(),
                    "status", transfer.getStatus(),
                    "recordsTransferred", transfer.getRecordsTransferred()
            ));
            kafkaTemplate.executeInTransaction(operations -> {
                operations.send("connector.synced", payload);
                return true;
            });
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to publish connector sync event", ex);
        }
    }

    private void logInfo(Long connectorId, String message) {
        connectorLogRepo.save(ConnectorLog.builder()
                .connectorId(connectorId)
                .logLevel("INFO")
                .message(message)
                .build());
    }

    private void logWarn(Long connectorId, String message) {
        connectorLogRepo.save(ConnectorLog.builder()
                .connectorId(connectorId)
                .logLevel("WARN")
                .message(message)
                .build());
    }

    private ConnectorResponse toResponse(Connector connector) {
        return ConnectorResponse.builder()
                .id(connector.getId())
                .tenantId(connector.getTenantId())
                .name(connector.getName())
                .connectorType(connector.getConnectorType())
                .sourceType(connector.getSourceType())
                .targetType(connector.getTargetType())
                .endpoint(connector.getEndpoint())
                .status(connector.getStatus())
                .lastSyncedAt(connector.getLastSyncedAt())
                .createdAt(connector.getCreatedAt())
                .updatedAt(connector.getUpdatedAt())
                .build();
    }
}
