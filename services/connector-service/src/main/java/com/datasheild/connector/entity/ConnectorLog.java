package com.datasheild.connector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connector_log", schema = "connector", indexes = {
        @Index(name = "idx_connector_log_connector_id", columnList = "connector_id"),
        @Index(name = "idx_connector_log_logged_at", columnList = "logged_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "connector_id", nullable = false)
    private Long connectorId;

    @Column(name = "log_level", nullable = false)
    private String logLevel;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    void onCreate() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
    }
}
