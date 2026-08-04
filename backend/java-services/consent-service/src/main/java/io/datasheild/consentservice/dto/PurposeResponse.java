package io.datasheild.consentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurposeResponse {

    private UUID id;

    private String purposeCode;

    private String purposeName;

    private String description;

    private Integer retentionDays;

    private String status;

    private Boolean requiresAudit;
}
