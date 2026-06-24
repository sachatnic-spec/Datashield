package com.datasheild.lineage.dto;

import com.datasheild.lineage.entity.DataFlow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataFlowRequest {
    private String sourceTable;
    private String sourceDatabase;
    private String targetTable;
    private String targetDatabase;
    private DataFlow.TransformationType transformationType;
    private Boolean isThirdPartySharing;
    private String thirdPartyName;
}
