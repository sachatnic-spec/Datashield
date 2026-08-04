package io.datasheild.auditservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.datasheild.auditservice.entity.AuditEvent;
import io.datasheild.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "consent-events",
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleConsentEvents(String message) {
        log.debug("Received consent event for audit: {}", message);
        try {
            AuditEvent event = AuditEvent.builder()
                    .tenantId(UUID.randomUUID())
                    .correlationId(UUID.randomUUID().toString())
                    .sourceService("consent-service")
                    .entityType("ConsentRecord")
                    .eventType("CONSENT_EVENT")
                    .entityId(UUID.randomUUID())
                    .eventPayload(message)
                    .actorId("system")
                    .build();

            auditService.ingestAuditEvent(event);
        } catch (Exception e) {
            log.error("Error processing consent event: {}", e.getMessage());
        }
    }

    @KafkaListener(
        topics = "dpr-events",
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDPREvents(String message) {
        log.debug("Received DPR event for audit: {}", message);
        try {
            AuditEvent event = AuditEvent.builder()
                    .tenantId(UUID.randomUUID())
                    .correlationId(UUID.randomUUID().toString())
                    .sourceService("rights-service")
                    .entityType("DPRRequest")
                    .eventType("DPR_EVENT")
                    .entityId(UUID.randomUUID())
                    .eventPayload(message)
                    .actorId("system")
                    .build();

            auditService.ingestAuditEvent(event);
        } catch (Exception e) {
            log.error("Error processing DPR event: {}", e.getMessage());
        }
    }

    @KafkaListener(
        topics = "breach-events",
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleBreachEvents(String message) {
        log.debug("Received breach event for audit: {}", message);
        try {
            AuditEvent event = AuditEvent.builder()
                    .tenantId(UUID.randomUUID())
                    .correlationId(UUID.randomUUID().toString())
                    .sourceService("breach-service")
                    .entityType("BreachIncident")
                    .eventType("BREACH_EVENT")
                    .entityId(UUID.randomUUID())
                    .eventPayload(message)
                    .actorId("system")
                    .build();

            auditService.ingestAuditEvent(event);
        } catch (Exception e) {
            log.error("Error processing breach event: {}", e.getMessage());
        }
    }
}
