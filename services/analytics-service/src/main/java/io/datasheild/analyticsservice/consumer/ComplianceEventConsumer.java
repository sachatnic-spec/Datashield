package io.datasheild.analyticsservice.consumer;

import io.datasheild.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComplianceEventConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "grievance-filed", groupId = "analytics-group")
    public void consumeGrievanceFiled(String message) {
        log.info("Consumed GrievanceFiledEvent: {}", message);
        analyticsService.recordMetric(
            UUID.randomUUID(),
            "GRIEVANCE_FILED",
            1.0,
            "count",
            "DPDP_18"
        );
    }

    @KafkaListener(topics = "grievance-resolved", groupId = "analytics-group")
    public void consumeGrievanceResolved(String message) {
        log.info("Consumed GrievanceResolvedEvent: {}", message);
        analyticsService.recordMetric(
            UUID.randomUUID(),
            "GRIEVANCE_RESOLVED",
            1.0,
            "count",
            "DPDP_18"
        );
    }

    @KafkaListener(topics = "sla-breach-alert", groupId = "analytics-group")
    public void consumeSLABreach(String message) {
        log.info("Consumed SLABreachAlertEvent: {}", message);
        analyticsService.recordMetric(
            UUID.randomUUID(),
            "SLA_BREACH_ALERT",
            1.0,
            "count",
            "DPDP_18"
        );
    }

    @KafkaListener(topics = "data-erasure-completed", groupId = "analytics-group")
    public void consumeDataErasure(String message) {
        log.info("Consumed DataErasureCompletedEvent: {}", message);
        analyticsService.recordMetric(
            UUID.randomUUID(),
            "DATA_ERASURE_COMPLETED",
            1.0,
            "count",
            "DPDP_11_12"
        );
    }

    @KafkaListener(topics = "vendor-onboarded", groupId = "analytics-group")
    public void consumeVendorOnboarded(String message) {
        log.info("Consumed VendorOnboardedEvent: {}", message);
        analyticsService.recordMetric(
            UUID.randomUUID(),
            "VENDOR_ONBOARDED",
            1.0,
            "count",
            "DPDP_9"
        );
    }

    @KafkaListener(topics = "policy-activated", groupId = "analytics-group")
    public void consumePolicyActivated(String message) {
        log.info("Consumed PolicyActivatedEvent: {}", message);
        analyticsService.recordMetric(
            UUID.randomUUID(),
            "POLICY_ACTIVATED",
            1.0,
            "count",
            "DPDP_4_7"
        );
    }
}
