package io.datasheild.breachservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.datasheild.breachservice.entity.BreachIncident;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BreachIncidentResponse {

    private UUID id;

    private BreachIncident.BreachStatus status;

    private BreachIncident.SeverityLevel severity;

    private String incidentTitle;

    private LocalDateTime discoveredAt;

    private LocalDateTime reportedAt;

    private LocalDateTime dpbiDeadline;

    private LocalDateTime dpbiNotifiedAt;

    private LocalDateTime dpNotifiedAt;

    private LocalDateTime containedAt;

    private Integer estimatedDataSubjects;

    private Boolean lossOfConfidentiality;

    private Boolean lossOfIntegrity;

    private Boolean lossOfAvailability;
}
