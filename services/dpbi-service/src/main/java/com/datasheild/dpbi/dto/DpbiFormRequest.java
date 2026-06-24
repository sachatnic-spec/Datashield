package com.datasheild.dpbi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DpbiFormRequest {
    private String tenantId;
    private Long breachId;
    private LocalDate discoveryDate;
    private String incidentSummary;
    private String impactAssessment;
    private String remediationPlan;
    private Integer affectedDataSubjects;
    private String dataCategories;
}
