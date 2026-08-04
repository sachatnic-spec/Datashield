package io.datasheild.breachservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.datasheild.breachservice.entity.BreachIncident;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportBreachRequest {

    private String incidentTitle;

    private String incidentDescription;

    private String affectedSystems;

    private Integer estimatedDataSubjects;

    private Integer estimatedRecords;

    private String dataCategories;  // JSON array

    private Boolean lossOfConfidentiality;

    private Boolean lossOfIntegrity;

    private Boolean lossOfAvailability;

    public boolean isValid() {
        return incidentTitle != null && !incidentTitle.isEmpty() &&
               estimatedDataSubjects != null && estimatedDataSubjects > 0;
    }
}
