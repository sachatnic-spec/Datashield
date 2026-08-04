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
public class SeverityScoreResponse {

    private UUID id;

    private Integer baseScore;

    private Integer adjustedScore;

    private BreachIncident.SeverityLevel proposedSeverity;

    private String scoringRationale;

    private LocalDateTime calculatedAt;

    private LocalDateTime reviewedAt;

    private String reviewedBy;
}
