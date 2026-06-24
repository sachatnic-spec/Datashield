package com.datasheild.piidetection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DetectionRequest {
    @NotNull
    private UUID tenantId;

    @NotBlank
    private String text;

    private String context;
}
