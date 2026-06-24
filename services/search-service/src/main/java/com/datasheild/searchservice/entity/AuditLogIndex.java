package com.datasheild.searchservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_log_index", schema = "search", indexes = {
        @Index(name = "idx_audit_index_tenant", columnList = "tenant_id"),
        @Index(name = "idx_audit_index_status", columnList = "status"),
        @Index(name = "idx_audit_index_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_index_tenant_status", columnList = "tenant_id,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "event_id", nullable = false, length = 120)
    private String eventId;

    @Column(name = "index_name", nullable = false, length = 160)
    private String indexName;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "compliance_score", precision = 10, scale = 2)
    private BigDecimal complianceScore;

    @Lob
    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
