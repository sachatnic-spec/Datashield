package com.datasheild.dpbi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FormReviewRequest {
    @NotBlank
    private String reviewedBy;
    private String comments;
}
