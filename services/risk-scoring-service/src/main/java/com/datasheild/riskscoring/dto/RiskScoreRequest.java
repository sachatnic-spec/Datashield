package com.datasheild.riskscoring.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RiskScoreRequest {
    @NotNull
    private UUID tenantId;

    @NotNull
    private UUID vendorId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double security;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double compliance;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double operational;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double historical;
}
