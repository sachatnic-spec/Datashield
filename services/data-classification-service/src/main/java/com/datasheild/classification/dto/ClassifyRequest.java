package com.datasheild.classification.dto;

import com.datasheild.classification.entity.DataClassification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassifyRequest {
    private String dataSetName;
    private String tableName;
    private Integer recordCount;
    private Integer piiFieldCount;
    private DataClassification.DataOwnershipType ownershipType;
}
