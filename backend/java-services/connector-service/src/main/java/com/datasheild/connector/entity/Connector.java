package com.datasheild.connector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connector", schema = "connector", indexes = {
        @Index(name = "idx_connector_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_connector_status", columnList = "status"),
        @Index(name = "idx_connector_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(name = "connector_type", nullable = false)
    private String connectorType;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "target_type")
    private String targetType;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String status;

    @Column(name = "credentials_encrypted", length = 4096)
    private String credentialsEncrypted;

    @Column(name = "configuration_json", length = 4096)
    private String configurationJson;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
