package io.datasheild.retentionservice.event;

import io.datasheild.common.event.DataShieldEvents;
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

        DataShieldEvents.WorkflowCompletedEvent event = DataShieldEvents.WorkflowCompletedEvent.builder()
            .workflowId(taskId)
            .workflowType("DATA_RETENTION_SCHEDULED")
            .status("SCHEDULED")
            .completedAt(LocalDateTime.now())
            .correlationId(correlationId)
            .build();

        kafkaTemplate.send("data-retention-scheduled", taskId.toString(), event)
            .addCallback(
                result -> log.info("DataRetentionScheduledEvent published: {}", taskId),
                ex -> log.error("Failed to publish event: {}", taskId, ex)
            );
    }

    public void publishDataErasureCompleted(UUID taskId, UUID tenantId, Integer recordsErased, 
                                           String archiveLocation, String correlationId) {
        log.info("Publishing DataErasureCompletedEvent: {}", taskId);

        DataShieldEvents.WorkflowCompletedEvent event = DataShieldEvents.WorkflowCompletedEvent.builder()
            .workflowId(taskId)
            .workflowType("DATA_ERASURE_COMPLETED")
            .status("COMPLETED")
            .completedAt(LocalDateTime.now())
            .correlationId(correlationId)
            .build();

        kafkaTemplate.send("data-erasure-completed", taskId.toString(), event)
            .addCallback(
                result -> log.info("DataErasureCompletedEvent published: {}", taskId),
                ex -> log.error("Failed to publish event: {}", taskId, ex)
            );
    }
}
