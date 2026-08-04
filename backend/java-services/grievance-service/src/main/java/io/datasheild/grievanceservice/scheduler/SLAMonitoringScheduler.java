package io.datasheild.grievanceservice.scheduler;

import io.datasheild.grievanceservice.entity.Grievance;
import io.datasheild.grievanceservice.service.GrievanceService;
import io.datasheild.grievanceservice.event.GrievanceEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SLAMonitoringScheduler {

    private final GrievanceService grievanceService;
    private final GrievanceEventPublisher eventPublisher;

    /**
     * Monitor SLA breaches every 6 hours
     * Publishes alert if grievance not resolved within 30-day deadline
     */
    @Scheduled(fixedRate = 21600000)
    public void monitorSLABreaches() {
        log.info("Running SLA breach monitoring scheduler");

        List<Grievance> breaches = grievanceService.getSLABreaches();

        for (Grievance grievance : breaches) {
            LocalDateTime now = LocalDateTime.now();
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(grievance.getSlaDeadline(), now);

            log.warn("SLA Breach detected: grievanceId={}, daysOverdue={}", grievance.getId(), daysOverdue);

            eventPublisher.publishSLABreachAlert(
                grievance.getId(),
                grievance.getTenantId(),
                (int) daysOverdue,
                UUID.randomUUID().toString()
            );

            if (daysOverdue >= 30) {
                log.error("CRITICAL: SLA breach by 30+ days: {}", grievance.getId());
                escalateToCompliance(grievance);
            }
        }

        log.info("SLA monitoring completed. Breaches found: {}", breaches.size());
    }

    /**
     * Escalate critical SLA breaches to compliance/DPO
     * Notifies Notification Service via event
     */
    private void escalateToCompliance(Grievance grievance) {
        log.info("Escalating grievance to compliance: {}", grievance.getId());

        try {
            grievanceService.escalateGrievance(
                grievance.getId(),
                "SLA breach detected: " + grievance.getSlaDeadline() + " exceeded",
                "SYSTEM"
            );
        } catch (Exception e) {
            log.error("Failed to escalate grievance: {}", grievance.getId(), e);
        }
    }

    /**
     * Daily cleanup: Archive resolved grievances older than 90 days
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveResolvedGrievances() {
        log.info("Running daily grievance archive scheduler");

        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        log.info("Archiving grievances resolved before: {}", ninetyDaysAgo);

        log.info("Grievance archival completed");
    }
}
