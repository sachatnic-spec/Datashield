package io.datasheild.workflowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiateWorkflowRequest {

    private String workflowType;
    private String entityType;
    private UUID entityId;
    private String initiatedBy;
    private String contextData;
    private Integer maxRetries;
    private Integer timeoutMinutes;
    private List<StepDefinition> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepDefinition {
        private String stepName;
        private String stepType;
        private String actionData;
        private Boolean requiresApproval;
    }
}
