package com.datasheild.siem.entity;

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
@Table(name = "siem_integration", schema = "siem", indexes = {
        @Index(name = "idx_siem_integration_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_siem_integration_type", columnList = "integration_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiemIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "integration_type", nullable = false)
    private String integrationType;

    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "auth_token_encrypted", length = 2000)
    private String authTokenEncrypted;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_delivered_at")
    private LocalDateTime lastDeliveredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
