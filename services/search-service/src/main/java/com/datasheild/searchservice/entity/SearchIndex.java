package com.datasheild.searchservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_index", schema = "search", indexes = {
        @Index(name = "idx_search_index_tenant", columnList = "tenant_id"),
        @Index(name = "idx_search_index_status", columnList = "status"),
        @Index(name = "idx_search_index_created_at", columnList = "created_at"),
        @Index(name = "idx_search_index_tenant_status", columnList = "tenant_id,status")
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SearchIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "index_name", nullable = false, length = 160)
    private String indexName;

    @Column(name = "document_id", nullable = false, length = 120)
    private String documentId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "document_type", length = 64)
    private String documentType;

    @Column(name = "compliance_score", precision = 10, scale = 2)
    private BigDecimal complianceScore;

    @Column(name = "event_timestamp")
    private Instant eventTimestamp;

    @Lob
    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
