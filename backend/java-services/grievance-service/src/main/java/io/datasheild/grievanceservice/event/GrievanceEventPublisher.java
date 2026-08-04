package io.datasheild.grievanceservice.event;

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
public class GrievanceEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishGrievanceFiled(UUID grievanceId, UUID tenantId, UUID dataPrincipalId, 
                                     String category, String correlationId) {
        log.info("Publishing GrievanceFiledEvent: {}", grievanceId);

        WorkflowCompletedEvent event = WorkflowCompletedEvent.builder()
            .workflowId(grievanceId)
            .workflowType("GRIEVANCE_FILED")
            .status("FILED")
            .completedAt(LocalDateTime.now())
            .correlationId(correlationId)
            .build();

        kafkaTemplate.send("grievance-filed", grievanceId.toString(), event);
        log.info("GrievanceFiledEvent published: {}", grievanceId);
    }

    public void publishGrievanceResolved(UUID grievanceId, UUID tenantId, String resolution, String correlationId) {
        log.info("Publishing GrievanceResolvedEvent: {}", grievanceId);

        WorkflowCompletedEvent event = WorkflowCompletedEvent.builder()
            .workflowId(grievanceId)
            .workflowType("GRIEVANCE_RESOLVED")
            .status("RESOLVED")
            .completedAt(LocalDateTime.now())
            .correlationId(correlationId)
            .build();

        kafkaTemplate.send("grievance-resolved", grievanceId.toString(), event);
        log.info("GrievanceResolvedEvent published: {}", grievanceId);
    }

    public void publishSLABreachAlert(UUID grievanceId, UUID tenantId, Integer daysOverdue, String correlationId) {
        log.info("Publishing SLABreachAlertEvent: {}", grievanceId);

        WorkflowCompletedEvent event = WorkflowCompletedEvent.builder()
            .workflowId(grievanceId)
            .workflowType("SLA_BREACH_ALERT")
            .status("ALERT")
            .completedAt(LocalDateTime.now())
            .correlationId(correlationId)
            .build();

        kafkaTemplate.send("sla-breach-alert", grievanceId.toString(), event);
        log.info("SLABreachAlertEvent published: {}", grievanceId);
    }
}
