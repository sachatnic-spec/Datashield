package io.datasheild.breachservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "containment_actions", schema = "breach",
       indexes = {
           @Index(name = "idx_action_breach", columnList = "breach_incident_id"),
           @Index(name = "idx_action_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContainmentAction {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID breachIncidentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionStatus status;

    @Column(length = 200, nullable = false)
    private String actionTitle;

    @Column(columnDefinition = "TEXT")
    private String actionDescription;

    @Column(length = 100)
    private String ownerTeam;  // DPO, Security, Engineering, etc

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime targetDeadline;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum ActionStatus {
        PLANNED,      // Planned containment action
        IN_PROGRESS,  // Currently being executed
        COMPLETED,    // Completed successfully
        FAILED        // Failed (requires escalation)
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
