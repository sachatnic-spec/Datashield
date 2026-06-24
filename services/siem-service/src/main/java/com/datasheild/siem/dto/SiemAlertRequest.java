package com.datasheild.siem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SiemAlertRequest {

    @NotBlank
    private String tenantId;
    @NotBlank
    private String alertType;
    @NotBlank
    private String severity;
    @NotBlank
    private String sourceSystem;
    private String externalIncidentId;
    private String message;
    private Double anomalyScore;
}
