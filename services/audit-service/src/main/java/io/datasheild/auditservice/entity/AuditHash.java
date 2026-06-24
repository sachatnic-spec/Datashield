package io.datasheild.auditservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "audit_hashes",
    schema = "audit",
    indexes = {
        @Index(name = "idx_hash_audit_log", columnList = "audit_log_id"),
        @Index(name = "idx_hash_tenant", columnList = "tenant_id"),
        @Index(name = "idx_hash_created", columnList = "created_at"),
        @Index(name = "idx_hash_sequence", columnList = "sequence_number")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditHash {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID auditLogId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private Long sequenceNumber;

    @Column(nullable = false)
    private String eventHash;

    @Column
    private String previousHash;

    @Column(nullable = false)
    private String chainHash;

    @Column
    private Boolean validChain;

    @Column
    private String validationStatus;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isValid() {
        return auditLogId != null && tenantId != null &&
               sequenceNumber != null && eventHash != null && !eventHash.isBlank() &&
               chainHash != null && !chainHash.isBlank();
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (validChain == null) {
            validChain = true;
        }
    }
}
