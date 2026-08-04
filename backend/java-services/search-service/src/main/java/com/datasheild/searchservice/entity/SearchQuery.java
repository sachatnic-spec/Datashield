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
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_query", schema = "search", indexes = {
        @Index(name = "idx_search_query_tenant", columnList = "tenant_id"),
        @Index(name = "idx_search_query_status", columnList = "status"),
        @Index(name = "idx_search_query_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "search_term", length = 255)
    private String searchTerm;

    @Column(name = "metric_type", length = 80)
    private String metricType;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "page_size", nullable = false)
    private Integer pageSize;

    @Column(name = "degraded", nullable = false)
    private boolean degraded;

    @Column(name = "total_hits", nullable = false)
    private long totalHits;

    @Lob
    @Column(name = "request_json", columnDefinition = "TEXT")
    private String requestJson;

    @Lob
    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
