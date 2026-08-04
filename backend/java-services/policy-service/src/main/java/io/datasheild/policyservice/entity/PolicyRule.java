package io.datasheild.policyservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "policy_rules", schema = "policy", indexes = {
    @Index(name = "idx_rule_policy_id", columnList = "policy_id"),
    @Index(name = "idx_rule_condition_type", columnList = "condition_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "policy_id", nullable = false, columnDefinition = "uuid")
    private UUID policyId;

    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    @Column(name = "condition_type", nullable = false, length = 100)
    private String conditionType;

    @Column(name = "condition_expression", nullable = false, columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(name = "action", nullable = false, length = 255)
    private String action;

    @Column(name = "action_params", columnDefinition = "jsonb")
    private String actionParams;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
