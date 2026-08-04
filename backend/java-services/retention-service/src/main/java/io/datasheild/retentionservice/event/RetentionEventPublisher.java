package io.datasheild.retentionservice.event;

import io.datasheild.common.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetentionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDataRetentionScheduled(UUID taskId, UUID tenantId, String dataCategory, 
                                             Integer recordCount, LocalDateTime scheduledFor, String correlationId) {
        log.info("Publishing DataRetentionScheduledEvent: {}", taskId);

        WorkflowCompletedEvent event = WorkflowCompletedEvent.builder()
            .workflowId(taskId)
            .workflowType("DATA_RETENTION_SCHEDULED")
            .status("SCHEDULED")
            .completedAt(LocalDateTime.now())
            .correlationId(correlationId)
            .build();

        kafkaTemplate.send("data-retention-scheduled", taskId.toString(), event);
        log.info("DataRetentionScheduledEvent published: {}", taskId);
    }

    public void publishDataErasureCompleted(UUID taskId, UUID tenantId, Integer recordsErased, 
                                           String archiveLocation, String correlationId) {
        log.info("Publishing DataErasureCompletedEvent: {}", taskId);

        WorkflowCompletedEvent event = WorkflowCompletedEvent.builder()
            .workflowId(taskId)
            .workflowType("DATA_ERASURE_COMPLETED")
            .status("COMPLETED")
            .completedAt(LocalDateTime.now())
            .correlationId(correlationId)
            .build();

        kafkaTemplate.send("data-erasure-completed", taskId.toString(), event);
        log.info("DataErasureCompletedEvent published: {}", taskId);
    }
}
