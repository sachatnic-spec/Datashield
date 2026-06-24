package com.datasheild.piidetection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RedactionRequest {
    @NotBlank
    private String text;
}
