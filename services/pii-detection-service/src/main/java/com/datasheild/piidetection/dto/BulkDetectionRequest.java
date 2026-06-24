package com.datasheild.piidetection.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkDetectionRequest {
    @NotNull
    private UUID tenantId;

    @NotEmpty
    private List<String> texts;

    private String context;
}
