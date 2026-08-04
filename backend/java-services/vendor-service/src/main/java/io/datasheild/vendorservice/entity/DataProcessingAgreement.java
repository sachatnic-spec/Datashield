package io.datasheild.vendorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "data_processing_agreements", schema = "vendor", indexes = {
    @Index(name = "idx_dpa_vendor_id", columnList = "vendor_id"),
    @Index(name = "idx_dpa_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataProcessingAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "vendor_id", nullable = false, columnDefinition = "uuid")
    private UUID vendorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DPAStatus status;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "signed_date")
    private LocalDateTime signedDate;

    @Column(name = "signed_by", length = 255)
    private String signedBy;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "renewal_date")
    private LocalDateTime renewalDate;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "data_categories", columnDefinition = "TEXT")
    private String dataCategories;

    @Column(name = "sub_processors_allowed", nullable = false)
    private Boolean subProcessorsAllowed = false;

    @Column(name = "approved_sub_processors", columnDefinition = "TEXT")
    private String approvedSubProcessors;

    @Column(name = "scc_included", nullable = false)
    private Boolean sccIncluded = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = DPAStatus.DRAFT;
        }
    }

    public enum DPAStatus {
        DRAFT,
        PENDING_SIGNATURE,
        EXECUTED,
        RENEWAL_PENDING,
        EXPIRED,
        TERMINATED
    }
}
