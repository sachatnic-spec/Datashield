package io.datasheild.grievanceservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grievance_activity", schema = "grievance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceActivity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID grievanceId;

    @Column(nullable = false, length = 100)
    private String activityType;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String performedBy;

    @Column(length = 50)
    private String statusBefore;

    @Column(length = 50)
    private String statusAfter;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
