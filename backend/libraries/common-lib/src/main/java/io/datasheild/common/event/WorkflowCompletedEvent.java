package io.datasheild.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowCompletedEvent {
    private UUID workflowId;
    private String workflowType;
    private String status;
    private LocalDateTime completedAt;
    private String correlationId;
}
