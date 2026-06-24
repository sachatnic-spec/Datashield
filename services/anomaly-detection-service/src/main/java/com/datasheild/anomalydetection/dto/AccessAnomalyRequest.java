package com.datasheild.anomalydetection.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccessAnomalyRequest {
    @NotNull
    private UUID userId;

    @NotNull
    private LocalDateTime accessTime;

    @NotBlank
    private String location;

    @NotNull
    @Min(1)
    private Integer volume;
}
