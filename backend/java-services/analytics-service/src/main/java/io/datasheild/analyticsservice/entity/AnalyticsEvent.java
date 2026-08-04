package io.datasheild.analyticsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analytics_event", schema = "analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEvent {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private Integer eventCount = 1;

    @Column(length = 2000)
    private String eventDetails;

    @Column(length = 255)
    private String correlationId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
