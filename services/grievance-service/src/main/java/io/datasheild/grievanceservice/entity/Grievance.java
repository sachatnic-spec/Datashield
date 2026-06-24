package io.datasheild.grievanceservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grievance", schema = "grievance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grievance {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID dataPrincipalId;

    @Column(nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private GrievanceCategory category;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private GrievanceChannel channel;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    private GrievanceStatus status = GrievanceStatus.FILED;

    @Column(length = 100)
    private String priority;

    @Column(nullable = false)
    private LocalDateTime filedAt;

    @Column(nullable = false)
    private LocalDateTime slaDeadline;

    private LocalDateTime investigationStartedAt;

    private LocalDateTime resolvedAt;

    @Column(length = 255)
    private String assignedTo;

    @Column(length = 2000)
    private String resolution;

    @Column(length = 500)
    private String escalationReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum GrievanceCategory {
        DATA_ACCESS_DENIAL, CORRECTION_DENIAL, ERASURE_DENIAL, PORTABILITY_DENIAL,
        SLA_BREACH, UNAUTHORIZED_ACCESS, DATA_BREACH, PRIVACY_VIOLATION,
        THIRD_PARTY_COMPLAINT, OTHER
    }

    public enum GrievanceChannel {
        WEB, EMAIL, PHONE, WHATSAPP, PHYSICAL_LETTER
    }

    public enum GrievanceStatus {
        FILED, ACKNOWLEDGED, INVESTIGATING, AWAITING_INFO, RESOLVED, REJECTED, ESCALATED
    }
}
