package com.datasheild.connector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_transfer", schema = "connector", indexes = {
        @Index(name = "idx_data_transfer_connector_id", columnList = "connector_id"),
        @Index(name = "idx_data_transfer_status", columnList = "status"),
        @Index(name = "idx_data_transfer_started_at", columnList = "started_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "connector_id", nullable = false)
    private Long connectorId;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "target_type")
    private String targetType;

    @Column(nullable = false)
    private String status;

    @Column(name = "records_transferred")
    private Long recordsTransferred;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
